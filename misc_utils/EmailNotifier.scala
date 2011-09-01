/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on: 1st September 2011
 */
package notifiers
import play.exceptions.MailException

import java.util.concurrent.Future

import javax.mail.internet.InternetAddress

import scala.collection.JavaConversions._

import org.apache.commons.mail._

import play.classloading.enhancers.LocalvariablesNamesEnhancer.{ LocalVariablesSupport, LocalVariablesNamesTracer }
import play.data.validation.Required
import play.data.validation.RequiredCheck
import play.exceptions.TemplateNotFoundException
import play.libs.Mail
import play.templates.TemplateLoader

/**
 * This trait provides a wrapper for sending email messages.
 *
 * @author Aishwarya Singhal
 */
trait EmailNotifier extends LocalVariablesSupport {

  @Required
  var subject: String = null
  @Required
  var recipients = List[String]()
  @Required
  var from: String = null
  var ccRecipients = List[String]()
  var bccRecipients = List[String]()
  var attachments = List[EmailAttachment]()
  var contentType: String = null
  var replyTo: String = null
  var charset: String = "utf-8"
  var headers = Map[String, String]()

  /**
   * Sets a subject for this email. It enables formatting of the providing string using Java's
   * string formatter.
   *
   * @param subject
   * @param args
   */
  def setSubject(subject: String, args: AnyRef*) = {
    this.subject = String.format(subject, args: _*)
  }

  /**
   * Adds an email recipient ("to" addressee).
   *
   * @param recipients
   */
  def addRecipient(recipients: String*) = {
    this.recipients = this.recipients ::: List(recipients: _*)
  }

  /**
   * Defines the sender of this email("from" address).
   *
   * @param from
   */
  def addFrom(from: String) = {
    this.from = from
  }

  /**
   * Adds an email recipient in CC.
   *
   * @param ccRecipients
   */
  def addCc(ccRecipients: String*) = {
    this.ccRecipients = this.ccRecipients ::: List(ccRecipients: _*)
  }

  /**
   * Adds an email recipient in BCC.
   *
   * @param bccRecipients
   */
  def addBcc(bccRecipients: String*) = {
    this.bccRecipients = this.bccRecipients ::: List(bccRecipients: _*)
  }

  /**
   * Adds an attachment to this email.
   *
   * @param attachments
   */
  def addAttachment(attachments: EmailAttachment*) = {
    this.attachments = this.attachments ::: List(attachments).asInstanceOf[List[EmailAttachment]]
  }

  /**
   * Sets the content type for the email. If none is set, by default it is assumed to be "UTF-8".
   * @param contentType
   */
  def setContentType(contentType: String) = {
    this.contentType = this.contentType
  }

  /**
   * Sets the charset for this email.
   *
   * @param charset
   */
  def setCharset(charset: String) = {
    this.charset = this.charset
  }

  /**
   * Defines the "reply to" email address.
   *
   * @param replyTo
   */
  def setReplyTo(replyTo: String) = {
    this.replyTo = replyTo
  }

  /**
   * Adds a request header to this email message.
   *
   * @param key
   * @param value
   */
  def addHeader(key: String, value: String) = {
    headers += (key -> value)
  }

  /**
   * <p>Dispatches an email based on the provided data. It also validates and ensures completeness of
   * this object before attempting a send.
   * <p>The email body is built using the provided template and arguments (It uses the Play Java
   * template framework in the background).
   *
   * @param templateName : Full path and name of the template (minus the extension) as relative to
   * the views directory. It expects forward slashes ("/") in the path (even on Windows boxes).
   * @param args : Any arguments that must be bound to the template to generate the email body.
   * @return
   */
  def send(templateName: String, args: AnyRef*): Future[java.lang.Boolean] = {

    validate

    var templateHtmlBinding = new java.util.HashMap[String, Object]()
    var templateTextBinding = new java.util.HashMap[String, Object]()

    for (o <- args) {
      var names = LocalVariablesNamesTracer.getAllLocalVariableNames(o).toList
      for (name <- names) {
        templateHtmlBinding += (name -> o)
        templateTextBinding += (name -> o)
      }
    }

    val bodyHtml: String = getBody(templateName + ".html", templateHtmlBinding, contentType)
    var bodyText: String = getBody(templateName + ".txt", templateTextBinding, contentType)

    // Content type
    ensureContentTypeDefined(bodyHtml)

    var email: MultiPartEmail = getEmail(bodyText, bodyHtml)

    attachments.foreach(email.attach(_))

    email.setCharset(charset)

    setAddress(from) { (address, name) => email.setFrom(address, name) }
    setAddress(replyTo) { (address, name) => email.addReplyTo(address, name) }
    recipients.foreach(setAddress(_) { (address, name) => email.addTo(address, name) })
    ccRecipients.foreach(setAddress(_) { (address, name) => email.addCc(address, name) })
    bccRecipients.foreach(setAddress(_) { (address, name) => email.addBcc(address, name) })

    email.setSubject(subject)
    email.updateContentType(contentType)
    headers foreach ((entry) => email.addHeader(entry._1, entry._2))

    Mail.send(email)
  }

  /**
   * Validates this object for completeness. It only looks through fields annotated with
   * <code>@Required</code> and ensures a value is set to them.
   */
  private def validate = {
    var fields = this.getClass.getDeclaredFields
    for (field <- fields) {
      val ann = field.getAnnotation(classOf[Required])
      if (ann != null) {
        var access = field.isAccessible
        field.setAccessible(true)
        val value = field.get(this)
        field.setAccessible(access)
        if (!new RequiredCheck().isSatisfied(null, value, null, null)) {
          throw new MailException(field.getName + " is required (but not provided).")
        }
		
        if (classOf[Seq[_]].isAssignableFrom(field.getType)) {
          var s = value.asInstanceOf[Seq[_]]
          if (s.size == 0) {
            throw new MailException(field.getName + " is required (but not provided).")
          }
        }
      }
    }
  }

  /**
   * Extracts an email address from the given string and passes to the enclosed method.
   *
   * @param emailAddress
   * @param setter
   */
  private def setAddress(emailAddress: String)(setter: (String, String) => Unit) = {

    if (emailAddress != null) {
      try {
        val iAddress = new InternetAddress(emailAddress);
        val address = iAddress.getAddress()
        val name = iAddress.getPersonal()

        setter(address, name)
      } catch {
        case e: Exception =>
          setter(emailAddress, null)
      }
    }
  }

  /**
   * Creates an appropriate email object based on the content type.
   *
   * @param bodyText
   * @param bodyHtml
   * @return
   */
  private def getEmail(bodyText: String, bodyHtml: String): MultiPartEmail = {
    var email: MultiPartEmail = null
    if (bodyHtml == null) {
      email = new MultiPartEmail();
      email.setMsg(bodyText);
    } else {
      email = new HtmlEmail();
      email.asInstanceOf[HtmlEmail].setHtmlMsg(bodyHtml);
      if (bodyText != null) {
        email.asInstanceOf[HtmlEmail].setTextMsg(bodyText);
      }
    }
    return email
  }

  /**
   * Sets a content type if none is defined.
   *
   * @param bodyHtml
   */
  private def ensureContentTypeDefined(bodyHtml: String) = {
    if (contentType == null) {
      if (bodyHtml != null) {
        contentType = "text/html";
      } else {
        contentType = "text/plain";
      }
    }
  }

  /**
   * Gets the body of this email from a given template.
   * @param templateName
   * @param templateBinding
   * @param contentType
   * @return
   */
  private def getBody(templateName: String, templateBinding: java.util.HashMap[String, Object], contentType: String): String = {
    var body: String = null

    try {
      val template = TemplateLoader.load(templateName)
      body = template.render(templateBinding)
    } catch {
      case e: TemplateNotFoundException =>
        if (this.contentType != null && this.contentType != contentType) {
          throw e;
        }
    }

    return body
  }
}