# Email Notification Testing Guide

## 🎯 Overview

This guide shows you **5 different ways** to test if your email notification system is working correctly.

---

## ✅ Method 1: Test Email Endpoint (EASIEST - Recommended)

### Step 1: Deploy the Test Endpoint

The `HealthController` now includes a `/api/test-email` endpoint for testing.

**Commit and push:**
```bash
cd C:\Users\firas\Desktop\IntegrationProject\IntegrationProjectBackend
git add .
git commit -m "Add email testing endpoint"
git push origin main
```

Wait 5-10 minutes for Render to deploy.

### Step 2: Send Test Email

**Option A: Test with your email**
```
https://integrationprojectbackend.onrender.com/api/test-email?to=your-email@gmail.com
```

**Option B: Test without parameters (uses default)**
```
https://integrationprojectbackend.onrender.com/api/test-email
```

### Step 3: Check Response

**Success Response:**
```json
{
  "success": true,
  "message": "Test email sent successfully!",
  "recipient": "your-email@gmail.com",
  "timestamp": "2025-11-16T14:30:00",
  "note": "Check your inbox (and spam folder) for the test email"
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Failed to send email",
  "error": "Authentication failed",
  "timestamp": "2025-11-16T14:30:00",
  "troubleshooting": "Check that EMAIL_USERNAME and EMAIL_APP_PASSWORD are set..."
}
```

### Step 4: Check Your Email Inbox

Look for email with subject:
```
✅ Test Email from IntegrationProject Backend
```

**If not in inbox, check:**
- ✉️ Spam/Junk folder
- 📧 Promotions tab (Gmail)
- 🗑️ Trash folder

---

## 🔍 Method 2: Check Render Logs (Real-Time Monitoring)

### How to Access Logs:

1. Go to **https://dashboard.render.com/**
2. Click on **IntegrationProjectBackend** service
3. Click **"Logs"** tab (left sidebar)
4. Logs update in real-time

### What to Look For:

**✅ Successful Email:**
```
✅ Email sent to: student@example.com
✅ Reminder email sent to: student@example.com
✅ Revision alert sent to parent: parent@example.com
✅ Quiz alert sent to parent: parent@example.com
```

**❌ Email Errors:**
```
❌ Failed to send email to student@example.com: Authentication failed
❌ Failed to send parent alert to parent@example.com: Connection timeout
```

**🔔 Scheduler Running:**
```
🔔 [2025-11-16T22:00:00] Running homework reminder check...
✅ Sent reminder to John Doe (3 tasks)
✅ Homework reminder check completed
```

**📊 Email Service Activity:**
```
[EmailService] Sending email to: test@example.com
[EmailService] Subject: ⏰ Homework Reminder
[EmailService] Email sent successfully
```

### Filter Logs:

In the search box, filter by:
- `EmailService` - See all email activity
- `✅` - See only successful operations
- `❌` - See only errors
- `🔔` - See scheduler activity

---

## 📅 Method 3: Wait for Scheduled Tasks (Production Test)

This tests the **automatic** notification system in production.

### Homework Reminder Test (10 PM Daily)

**Setup:**
1. Create a student account (or use existing)
2. Generate a schedule with tasks for today
3. **Don't complete** some tasks
4. Wait until **10:00 PM** (server time)

**Expected Result:**
- At 10:00 PM, scheduler runs automatically
- Student receives email with incomplete tasks
- Render logs show: `🔔 Running homework reminder check...`

**Check:**
- Student's email inbox
- Render logs at 22:00 (10 PM)

### Revision Alert Test (11 PM Daily)

**Setup:**
1. Student has generated schedule for today
2. Complete less than 50% of tasks
3. Ensure student has parent linked
4. Wait until **11:00 PM**

**Expected Result:**
- At 11:00 PM, scheduler checks completion
- Parent receives alert email if < 50% done
- Render logs show: `⚠️ Sent revision alert to parent...`

**Check:**
- Parent's email inbox
- Render logs at 23:00 (11 PM)

### Quiz Score Test (Every Hour)

**Setup:**
1. Student completes a quiz
2. Quiz is marked with score
3. Wait up to 1 hour

**Expected Result:**
- Within 1 hour, parent receives quiz notification
- Email shows score and performance feedback
- Render logs show: `✅ Sent quiz notification to parent...`

**Check:**
- Parent's email inbox
- Render logs (hourly on the hour: 14:00, 15:00, etc.)

---

## 🛠️ Method 4: Local Testing (Before Deployment)

Test emails on your local machine before pushing to production.

### Step 1: Set Up Local Environment

Create/update `.env` file:
```env
EMAIL_USERNAME=your-email@gmail.com
EMAIL_APP_PASSWORD=your-16-char-app-password
GROQ_API_KEY=your-groq-api-key

# Local database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/integrationproject
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your-password
```

### Step 2: Run Application Locally

```bash
cd C:\Users\firas\Desktop\IntegrationProject\IntegrationProjectBackend
mvnw spring-boot:run
```

### Step 3: Test Endpoint Locally

Open browser:
```
http://localhost:5069/api/test-email?to=your-email@gmail.com
```

### Step 4: Check Console Output

Look in your terminal/console for:
```
✅ Email sent to: your-email@gmail.com
```

Or errors:
```
❌ Failed to send email to your-email@gmail.com: [error details]
```

---

## 📧 Method 5: Direct Email Service Test (Advanced)

Create a dedicated test controller for development.

### Create TestController.java

File: `controllers/TestController.java`

```java
package com.example.IntegrationProjectBackend.controllers;

import com.example.IntegrationProjectBackend.services.EmailService;
import com.example.IntegrationProjectBackend.models.Student;
import com.example.IntegrationProjectBackend.models.Parent;
import com.example.IntegrationProjectBackend.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/email/simple")
    public ResponseEntity<String> testSimpleEmail(@RequestParam String to) {
        try {
            emailService.sendEmail(to, "Test", "This is a test email!");
            return ResponseEntity.ok("✅ Simple email sent to: " + to);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/email/student-reminder")
    public ResponseEntity<String> testStudentReminder(@RequestParam String email) {
        try {
            emailService.sendStudentReminder(
                email,
                "Test Student",
                "- 18:00-19:00: Math homework\n- 19:00-20:00: Physics revision"
            );
            return ResponseEntity.ok("✅ Student reminder sent to: " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/email/parent-revision")
    public ResponseEntity<String> testParentRevisionAlert(@RequestParam String email) {
        try {
            emailService.sendParentRevisionAlert(
                email,
                "Test Parent",
                "Test Student",
                35
            );
            return ResponseEntity.ok("✅ Parent revision alert sent to: " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/email/parent-quiz")
    public ResponseEntity<String> testParentQuizAlert(@RequestParam String email) {
        try {
            emailService.sendParentQuizAlert(
                email,
                "Test Parent",
                "Test Student",
                "Mathematics - Algebra",
                65.5,
                100.0
            );
            return ResponseEntity.ok("✅ Parent quiz alert sent to: " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }
}
```

### Usage Examples:

```
GET /api/test/email/simple?to=your-email@gmail.com
GET /api/test/email/student-reminder?email=student@example.com
GET /api/test/email/parent-revision?email=parent@example.com
GET /api/test/email/parent-quiz?email=parent@example.com
```

---

## 🚨 Troubleshooting

### Problem: "Authentication failed"

**Cause:** Email credentials not configured or incorrect

**Solution:**
1. Verify environment variables in Render:
   - `EMAIL_USERNAME` = your-email@gmail.com
   - `EMAIL_APP_PASSWORD` = 16-character app password
2. Make sure you're using **App Password**, not regular Gmail password
3. Verify 2FA is enabled on your Google account

### Problem: "Connection timeout"

**Cause:** SMTP server unreachable or port blocked

**Solution:**
1. Check `application.properties`:
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   ```
2. Ensure Render can access external SMTP servers (should work by default)
3. Try alternative port 465 with SSL

### Problem: Email not received

**Cause:** Email sent but filtered or blocked

**Solution:**
1. **Check spam folder** (most common issue!)
2. Check Promotions tab in Gmail
3. Add sender to contacts/safe list
4. Check email provider's blocked list
5. Verify recipient email address is correct

### Problem: "Email sent" in logs but not received

**Cause:** Email successfully sent to SMTP server but not delivered

**Solution:**
1. Check spam folder (90% of cases)
2. Verify recipient email is valid
3. Check Gmail sent folder to confirm it was sent
4. Wait a few minutes - delivery can be delayed
5. Try different email provider (not Gmail) to test

### Problem: No logs showing email activity

**Cause:** Scheduler not running or email service not configured

**Solutions:**
1. Verify `@EnableScheduling` in main application class
2. Check service is not sleeping (use UptimeRobot)
3. Verify environment variables are set
4. Check for startup errors in logs

---

## 📊 Expected Log Output Examples

### Successful Homework Reminder (10 PM)

```
🔔 [2025-11-16T22:00:00.123] Running homework reminder check...
✅ Sent reminder to John Doe (john@example.com) - 3 tasks incomplete
✅ Sent reminder to Jane Smith (jane@example.com) - 1 task incomplete
✅ Homework reminder check completed
Total: 2 emails sent
```

### Successful Revision Alert (11 PM)

```
🔔 [2025-11-16T23:00:00.456] Running revision adherence check for parents...
⚠️ Sent revision alert to parent of John Doe (completion: 35%)
✅ Revision adherence check completed
Total: 1 alert sent
```

### Successful Quiz Notification

```
🔔 [2025-11-16T15:00:00.789] Running quiz score check...
✅ Sent quiz notification to parent of John Doe (score: 85.0%)
⚠️ Sent quiz notification to parent of Jane Smith (score: 55.0%)
✅ Quiz score check completed
Total: 2 notifications sent
```

---

## ✅ Quick Testing Checklist

Use this checklist to verify everything works:

### Initial Setup
- [ ] Gmail App Password created
- [ ] `EMAIL_USERNAME` set in Render
- [ ] `EMAIL_APP_PASSWORD` set in Render
- [ ] Code deployed to Render successfully
- [ ] Service shows "Live" status

### Endpoint Testing
- [ ] `/api/health` returns 200 OK
- [ ] `/api/test-email` returns success response
- [ ] Test email received in inbox

### Log Verification
- [ ] Render logs show "✅ Email sent to..."
- [ ] No "❌" errors in logs
- [ ] Scheduler messages appear at scheduled times

### Production Testing
- [ ] Student account created with email
- [ ] Parent account linked with email
- [ ] Schedule generated with incomplete tasks
- [ ] Waited for 10 PM - email received
- [ ] Quiz completed - parent notified

### Monitoring
- [ ] UptimeRobot shows "Up" status
- [ ] Service not sleeping
- [ ] Scheduled tasks running on time

---

## 🎓 Best Practices

1. **Always check spam folder first** - Most "missing" emails are there
2. **Test with your own email** - Easiest way to verify
3. **Check logs immediately after testing** - See real-time results
4. **Use multiple email providers** - Test Gmail, Outlook, Yahoo, etc.
5. **Monitor at scheduled times** - Watch logs at 10 PM and 11 PM
6. **Keep UptimeRobot running** - Ensures service stays awake

---

## 📞 Need Help?

If emails still aren't working after all tests:

1. **Check Render Logs** - Look for specific error messages
2. **Verify Environment Variables** - Make sure they're set correctly
3. **Test with Different Email** - Try another email address
4. **Check Gmail Quota** - Free Gmail has 500 emails/day limit
5. **Review application.properties** - Ensure SMTP settings are correct

---

## 🚀 Next Steps

After successful testing:

1. ✅ Verify all 3 notification types work
2. ✅ Add your real student/parent emails to the system
3. ✅ Monitor logs for first few days
4. ✅ Consider upgrading to Render paid plan for 24/7 uptime
5. ✅ Set up email alert contacts in UptimeRobot

**Your automated notification system is ready!** 🎉
