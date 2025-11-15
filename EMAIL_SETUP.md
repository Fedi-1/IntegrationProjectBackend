# Email Notification System Setup Guide

## Overview
The notification system sends automated emails to students and parents based on:
- **Student Reminders**: Daily at 10 PM for incomplete homework/tasks
- **Parent Alerts**: 
  - Daily at 11 PM for poor revision adherence (< 50% completion)
  - Hourly checks for quiz score notifications

## Email Configuration

### Step 1: Create Gmail App Password

Since you're using Gmail SMTP, you need to create an **App Password** (not your regular Gmail password):

1. Go to your Google Account: https://myaccount.google.com/
2. Click on **Security** in the left menu
3. Enable **2-Step Verification** if not already enabled
4. After enabling 2FA, scroll down to find **App passwords**
5. Click **App passwords**
6. Select **Mail** and **Other (Custom name)**
7. Name it "IntegrationProject Backend"
8. Click **Generate**
9. Copy the 16-character password (format: xxxx xxxx xxxx xxxx)

### Step 2: Add Environment Variables to Render

1. Go to your Render dashboard: https://dashboard.render.com/
2. Select your **IntegrationProjectBackend** service
3. Go to **Environment** tab
4. Add these new variables:

```
EMAIL_USERNAME = your-email@gmail.com
EMAIL_APP_PASSWORD = your-16-character-app-password
```

**Example:**
```
EMAIL_USERNAME = student.system@gmail.com
EMAIL_APP_PASSWORD = abcd efgh ijkl mnop
```

5. Click **Save Changes**
6. Render will automatically redeploy your backend

### Step 3: Verify Email Configuration (Local Testing)

If testing locally, create/update your `.env` file:

```env
GROQ_API_KEY=your_groq_api_key
EMAIL_USERNAME=your-email@gmail.com
EMAIL_APP_PASSWORD=your-app-password
```

## Scheduled Tasks

The system runs these automated checks:

| Task | Schedule | Description |
|------|----------|-------------|
| **Homework Reminders** | Daily at 10:00 PM | Checks incomplete tasks and emails students |
| **Revision Adherence** | Daily at 11:00 PM | Alerts parents if completion < 50% |
| **Quiz Notifications** | Every hour | Notifies parents of new quiz results |

### Cron Schedule Format
```
"0 0 22 * * *"  →  10:00 PM every day
"0 0 23 * * *"  →  11:00 PM every day
"0 0 * * * *"   →  Every hour on the hour
```

## Email Templates

### 1. Student Homework Reminder
**Subject:** ⏰ Homework Reminder - Don't Forget!

**Body:**
```
Hello [Student Name],

This is a friendly reminder that you have pending homework:

📚 Task: [Task Description]
- [Time]: [Activity] ([Subject]) - [Topic]

Please complete it before the deadline.

Good luck with your studies!

Best regards,
Your Study Management System
```

### 2. Parent Revision Alert
**Subject:** ⚠️ Revision Schedule Alert - [Student Name]

**Body:**
```
Dear [Parent Name],

We noticed that [Student Name] is not following the revision schedule properly.

📊 Current Completion Rate: [XX]%

Please encourage your child to stay on track with their study plan.

Best regards,
Your Study Management System
```

### 3. Parent Quiz Notification
**Subject:** 📝 Quiz Score Notification - [Student Name]

**Body:**
```
Dear [Parent Name],

✅/⚠️ [Student Name] has completed a quiz:

Quiz: [Subject] - [Topic]
Score: [X.X] / [100.0] ([XX]%)

[Feedback message based on score]

Best regards,
Your Study Management System
```

## Testing the System

### Test Scheduler (Development Only)

Uncomment this method in `NotificationScheduler.java` to test every minute:

```java
@Scheduled(cron = "0 * * * * *")
public void testScheduler() {
    System.out.println("✅ Scheduler is working at: " + LocalDateTime.now());
}
```

### Manual Email Test

You can create a test endpoint to manually trigger emails:

```java
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private EmailService emailService;
    
    @GetMapping("/email")
    public String testEmail() {
        emailService.sendEmail(
            "test@example.com",
            "Test Email",
            "If you receive this, email is working!"
        );
        return "Email sent!";
    }
}
```

Call: `GET https://integrationprojectbackend.onrender.com/api/test/email`

## Troubleshooting

### Problem: Emails not sending

**Check 1: Environment Variables**
- Verify `EMAIL_USERNAME` and `EMAIL_APP_PASSWORD` are set in Render
- Check Render logs: `System.out` will show email send attempts

**Check 2: Gmail App Password**
- Use App Password, not regular Gmail password
- Ensure 2-Factor Authentication is enabled
- Remove spaces from app password when copying

**Check 3: Less Secure Apps**
- Google no longer supports "Less Secure Apps"
- **Must use App Passwords** with 2FA

**Check 4: SMTP Settings**
- Host: `smtp.gmail.com`
- Port: `587` (STARTTLS)
- Auth: `true`

### Problem: Scheduler not running

**Check logs for:**
```
🔔 [timestamp] Running homework reminder check...
```

If not appearing:
1. Verify `@EnableScheduling` is on main application class
2. Check Render service is **not sleeping** (free tier sleeps after 15 min inactivity)
3. Make HTTP request every 10 minutes to keep service awake

## Production Considerations

### 1. Keep Render Service Awake
Render free tier sleeps after 15 minutes of inactivity. Options:
- Upgrade to paid plan ($7/month) for 24/7 uptime
- Use external cron service (e.g., cron-job.org) to ping your API every 10 minutes
- Use UptimeRobot (free) to monitor and keep service awake

Example ping endpoint:
```
GET https://integrationprojectbackend.onrender.com/api/health
```

### 2. Email Rate Limits
Gmail has sending limits:
- Free Gmail: 500 emails/day
- Google Workspace: 2,000 emails/day

If exceeding limits, consider:
- SendGrid (100 emails/day free)
- Mailgun (5,000 emails/month free)
- AWS SES (very cheap, pay-as-you-go)

### 3. Time Zone Configuration
Default timezone is UTC. To change:

Add to `application.properties`:
```properties
spring.jpa.properties.hibernate.jdbc.time_zone=Africa/Tunis
```

Or set in Render environment:
```
TZ=Africa/Tunis
```

### 4. Logging & Monitoring
Check Render logs for scheduler activity:
- Dashboard → Your Service → Logs
- Look for 🔔 emoji for scheduler runs
- ✅ for successful operations
- ❌ for errors

## Next Steps

1. ✅ Add `EMAIL_USERNAME` and `EMAIL_APP_PASSWORD` to Render
2. ✅ Commit and push code to GitHub (auto-deploys to Render)
3. ✅ Wait for deployment to complete
4. ✅ Check Render logs for scheduler messages
5. ✅ Test by creating incomplete tasks and waiting for 10 PM
6. ✅ Verify emails are received

## Alternative Email Providers

If Gmail doesn't work, you can use other providers:

### SendGrid (Recommended for production)
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=${SENDGRID_API_KEY}
```

### Mailgun
```properties
spring.mail.host=smtp.mailgun.org
spring.mail.port=587
spring.mail.username=${MAILGUN_USERNAME}
spring.mail.password=${MAILGUN_PASSWORD}
```

## Support

For issues:
1. Check Render logs: Dashboard → Service → Logs
2. Look for error messages with ❌
3. Verify environment variables are set correctly
4. Test Gmail App Password in separate email client first
