package com.example.IntegrationProjectBackend.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Email service using SendGrid API (works on Render free tier)
 * SendGrid doesn't use SMTP ports, so it bypasses Render's restrictions
 */
@Service
public class SendGridEmailService {

    @Value("${sendgrid.api.key:NOT_SET}")
    private String sendGridApiKey;

    @Value("${spring.mail.username:louhichioussama59@gmail.com}")
    private String fromEmail;

    public boolean isConfigured() {
        return sendGridApiKey != null && !sendGridApiKey.equals("NOT_SET") && !sendGridApiKey.isEmpty();
    }

    /**
     * Send email using SendGrid API
     */
    public boolean sendEmail(String toEmail, String subject, String body) {
        if (!isConfigured()) {
            System.err.println("❌ SendGrid not configured. Set SENDGRID_API_KEY environment variable.");
            return false;
        }

        try {
            Email from = new Email(fromEmail);
            Email to = new Email(toEmail);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println(
                        "✅ SendGrid email sent to: " + toEmail + " (Status: " + response.getStatusCode() + ")");
                return true;
            } else {
                System.err.println("❌ SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
                return false;
            }

        } catch (IOException e) {
            System.err.println("❌ Failed to send email via SendGrid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send revision ending reminder to student
     */
    public void sendRevisionEndingReminder(String toEmail, String studentName, String subject, String topic,
            int minutesLeft) {
        String topicText = (topic != null && !topic.isEmpty()) ? " on " + topic : "";
        String body = String.format(
                "Hello %s,\n\n" +
                        "⏰ Your revision session for %s%s is ending in %d minutes!\n\n" +
                        "Make sure to:\n" +
                        "✅ Complete your current topic\n" +
                        "✅ Review key points\n" +
                        "✅ Mark your session as finished when done\n\n" +
                        "Keep up the great work! 📚\n\n" +
                        "Best regards,\n" +
                        "Your Study Management System",
                studentName, subject, topicText, minutesLeft);

        sendEmail(toEmail, "⏰ Revision Session Ending Soon - " + subject, body);
    }

    /**
     * Send homework alert (to parent or student)
     */
    public void sendHomeworkAlert(String toEmail, String recipientName, String studentName,
            int unfinishedCount, String taskList) {
        String body = String.format(
                "Dear %s,\n\n" +
                        "⚠️ %s has %d task(s) that were not marked as finished today:\n\n" +
                        "%s\n" +
                        "Please remember to:\n" +
                        "• Complete all assigned tasks\n" +
                        "• Mark tasks as finished when done\n" +
                        "• Follow the revision schedule\n\n" +
                        "Regular completion tracking helps maintain good study habits.\n\n" +
                        "Best regards,\n" +
                        "Your Study Management System",
                recipientName, studentName, unfinishedCount, taskList);

        sendEmail(toEmail, "⚠️ Homework Not Completed - " + studentName, body);
    }

    /**
     * Send quiz score alert (to parent or student)
     */
    public void sendQuizAlert(String toEmail, String recipientName, String studentName, String quizTitle,
            double score, double maxScore) {
        double percentage = (score / maxScore) * 100;
        String emoji = percentage >= 70 ? "✅" : "⚠️";

        String body = String.format(
                "Dear %s,\n\n" +
                        "%s %s has completed a quiz:\n\n" +
                        "Quiz: %s\n" +
                        "Score: %.1f / %.1f (%.1f%%)\n\n" +
                        "%s\n\n" +
                        "Best regards,\n" +
                        "Your Study Management System",
                recipientName, emoji, studentName, quizTitle, score, maxScore, percentage,
                percentage >= 70 ? "Great job! Keep up the good work!"
                        : "Please review the material and practice more.");

        sendEmail(toEmail, "📝 Quiz Score Notification - " + studentName, body);
    }

    /**
     * Send password reset email with HTML template
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken, String frontendUrl) {
        if (!isConfigured()) {
            System.err.println("❌ SendGrid not configured. Set SENDGRID_API_KEY environment variable.");
            throw new RuntimeException("Email service not configured");
        }

        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        String htmlContent = buildPasswordResetEmailHtml(userName, resetLink);

        try {
            Email from = new Email(fromEmail, "Integration Project");
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, "🔐 Réinitialisation de votre mot de passe", to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Password reset email sent to: " + toEmail);
            } else {
                System.err.println("❌ SendGrid error: " + response.getStatusCode());
                throw new RuntimeException("Failed to send password reset email");
            }

        } catch (IOException ex) {
            System.err.println("❌ Error sending password reset email: " + ex.getMessage());
            throw new RuntimeException("Error sending email: " + ex.getMessage(), ex);
        }
    }

    /**
     * Send password changed confirmation email
     */
    public void sendPasswordChangedConfirmation(String toEmail, String userName) {
        if (!isConfigured()) {
            System.err.println("❌ SendGrid not configured.");
            return;
        }

        String htmlContent = buildPasswordChangedEmailHtml(userName);

        try {
            Email from = new Email(fromEmail, "Integration Project");
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, "✅ Votre mot de passe a été modifié", to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Password confirmation email sent to: " + toEmail);
            }

        } catch (IOException ex) {
            System.err.println("⚠️ Failed to send confirmation email: " + ex.getMessage());
        }
    }

    private String buildPasswordResetEmailHtml(String userName, String resetLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }"
                +
                "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                "        .button { display: inline-block; padding: 15px 30px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }"
                +
                "        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 10px; margin: 20px 0; }"
                +
                "        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #777; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🔐 Réinitialisation du mot de passe</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>Bonjour <strong>" + userName + "</strong>,</p>" +
                "            <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>" +
                "            <p>Cliquez sur le bouton ci-dessous pour définir un nouveau mot de passe :</p>" +
                "            <div style='text-align: center;'>" +
                "                <a href='" + resetLink + "' class='button'>Réinitialiser mon mot de passe</a>" +
                "            </div>" +
                "            <div class='warning'>" +
                "                <strong>⚠️ Important :</strong><br>" +
                "                - Ce lien est valable pendant <strong>30 minutes</strong><br>" +
                "                - Si vous n'avez pas demandé cette réinitialisation, ignorez cet email<br>" +
                "                - Ne partagez jamais ce lien avec personne" +
                "            </div>" +
                "            <p>Si le bouton ne fonctionne pas, copiez ce lien :</p>" +
                "            <p style='word-break: break-all; color: #667eea;'>" + resetLink + "</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2025 Integration Project. Tous droits réservés.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private String buildPasswordChangedEmailHtml(String userName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }"
                +
                "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                "        .success { background: #d4edda; border-left: 4px solid #28a745; padding: 10px; margin: 20px 0; }"
                +
                "        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #777; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>✅ Mot de passe modifié</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>Bonjour <strong>" + userName + "</strong>,</p>" +
                "            <div class='success'>" +
                "                <strong>✅ Succès !</strong><br>" +
                "                Votre mot de passe a été modifié avec succès." +
                "            </div>" +
                "            <p>Si vous n'êtes pas à l'origine de cette modification, contactez immédiatement l'administrateur.</p>"
                +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2025 Integration Project. Tous droits réservés.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
