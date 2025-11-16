# Notification System Troubleshooting Guide

## Current Status Analysis

Based on your schedule showing 15 sessions today (Saturday) with only 2 completed, the notification system should be working. Let's diagnose the issue.

## Notification Types & Triggers

### 1. **Revision Ending Reminders** (For Students)
- **Runs**: Every 15 minutes
- **Condition**: Sends when a study/revision session is ending in 5-15 minutes
- **Recipient**: Student email
- **Your Case**: Since most sessions haven't started yet, you should receive reminders as each session approaches its end time

### 2. **Unfinished Homework Alerts** (For Parents)
- **Runs**: Daily at 11:00 PM (23:00)
- **Condition**: Sends if student has uncompleted tasks for the day
- **Recipient**: Parent email
- **Your Case**: At 11 PM tonight, if sessions 3-15 are still not completed, parent will receive an alert listing all 13 unfinished tasks

### 3. **Quiz Score Notifications** (For Parents)
- **Runs**: Every hour
- **Condition**: Sends for quizzes completed in the last hour
- **Recipient**: Parent email
- **Your Case**: Only triggers when you complete quizzes

## Testing the Notification System

### Step 1: Test Email Connectivity

Open this URL in your browser (replace with your actual email):

```
https://integrationprojectbackend.onrender.com/api/test-email?to=YOUR-EMAIL@gmail.com
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Test email sent successfully",
  "recipient": "YOUR-EMAIL@gmail.com",
  "timestamp": "2025-11-16T..."
}
```

If this fails, the issue is with email configuration (Gmail App Password).

### Step 2: Manually Trigger Notification Checks

#### Check Homework Notifications (Don't wait until 11 PM):
```
https://integrationprojectbackend.onrender.com/api/trigger-notifications?type=homework
```

This will immediately check for unfinished tasks and send parent alert if you have incomplete sessions.

#### Check Revision Reminders:
```
https://integrationprojectbackend.onrender.com/api/trigger-notifications?type=revision
```

This checks if any sessions are ending in the next 5-15 minutes.

#### Check Quiz Notifications:
```
https://integrationprojectbackend.onrender.com/api/trigger-notifications?type=quiz
```

This checks for recently completed quizzes.

#### Run All Checks:
```
https://integrationprojectbackend.onrender.com/api/trigger-notifications?type=all
```

### Step 3: Check Render Logs

1. Go to: https://dashboard.render.com
2. Select your `IntegrationProjectBackend` service
3. Click "Logs" tab
4. Look for these messages:

```
🔔 [timestamp] Checking for revision sessions ending soon...
✅ Revision session check completed

🔔 [timestamp] Checking for unfinished homework...
⚠️ Sent unfinished homework alert to parent of [Student Name] (X tasks not completed)
✅ Unfinished homework check completed

🔔 [timestamp] Running quiz score check...
✅ Quiz score check completed
```

## Common Issues & Solutions

### Issue 1: No Emails Received at All

**Possible Causes:**
- Gmail App Password not set in Render environment variables
- Email configuration incorrect
- Emails going to spam folder

**Solutions:**
1. Check Render Environment Variables:
   - `EMAIL_USERNAME` = feditriki05@gmail.com
   - `EMAIL_APP_PASSWORD` = your 16-character app password

2. Check your spam folder in Gmail

3. Test with the `/api/test-email` endpoint

### Issue 2: Scheduled Tasks Not Running

**Possible Causes:**
- Render service was sleeping
- @EnableScheduling not active

**Solutions:**
1. Verify UptimeRobot is pinging `/api/health` every 5 minutes
2. Check Render logs for scheduled task messages
3. Manually trigger with `/api/trigger-notifications`

### Issue 3: Revision Reminders Not Received

**Why This Might Happen:**
- Sessions must be ending in 5-15 minutes
- Only checks every 15 minutes
- Only for uncompleted sessions

**Example Timeline:**
- Session: 11:20-12:05 (ends at 12:05)
- Reminder window: 11:50-12:00 (5-15 min before 12:05)
- If scheduler runs at 11:45 (before window): No email
- If scheduler runs at 12:00 (in window): Email sent
- If scheduler runs at 12:15 (after window): No email

### Issue 4: Parent Doesn't Receive Homework Alert

**Requirements:**
- Student must have Parent record linked (parentCin)
- Parent must have valid email
- At least one uncompleted task for the day
- Time must be 11:00 PM

**Check in Database:**
```sql
-- Verify student has parent
SELECT s.cin, s.first_name, s.parent_cin, p.email 
FROM students s 
LEFT JOIN parents p ON s.parent_cin = p.cin 
WHERE s.cin = 'YOUR-CIN';
```

## Immediate Action Plan

### For Testing Right Now:

1. **Test Email System:**
   ```
   Visit: https://integrationprojectbackend.onrender.com/api/test-email
   ```
   Check your inbox (and spam) for test email.

2. **Trigger Homework Alert:**
   ```
   Visit: https://integrationprojectbackend.onrender.com/api/trigger-notifications?type=homework
   ```
   Since you have 13 unfinished tasks, parent should receive alert immediately.

3. **Check Logs:**
   Go to Render dashboard → Logs to see what happens.

### For Tonight (11 PM):

The homework alert will automatically run. Make sure:
- Render service is awake (UptimeRobot should keep it up)
- Some tasks remain uncompleted
- Parent email is valid

### For Tomorrow:

As you approach session end times (e.g., 11:05 for session ending at 11:10), you should receive revision ending reminders.

## Expected Email Content

### Student Revision Reminder:
```
Subject: 📚 Revision Session Ending Soon!

Hi [Student Name],

Your revision session for [Subject Name] is ending soon!

📖 Topic: [Topic Name]
⏰ Ending in: [X] minutes

Make sure to complete your study goals before time runs out!

Good luck with your studies! 🎓
```

### Parent Homework Alert:
```
Subject: ⚠️ Unfinished Tasks Alert - [Student Name]

Dear [Parent Name],

[Student Name] has [X] unfinished tasks for today:

- 11:20-12:05: study (Méthodologie de Conception Objet) - [Topic]
- 12:15-13:00: study (SOA) - [Topic]
...

Please check with your child to ensure they complete their study schedule.
```

### Parent Quiz Notification:
```
Subject: 📊 Quiz Completed - [Student Name]

Dear [Parent Name],

[Student Name] has completed a quiz:

📝 Quiz: [Subject] - [Topic]
📊 Score: [X]%

Keep encouraging your child's progress!
```

## Quick Checklist

- [ ] Render service is deployed and running
- [ ] UptimeRobot monitoring `/api/health` (prevents sleep)
- [ ] Environment variables set: `EMAIL_USERNAME`, `EMAIL_APP_PASSWORD`
- [ ] `/api/test-email` sends successfully
- [ ] `/api/trigger-notifications?type=homework` sends parent alert
- [ ] Logs show scheduled tasks running
- [ ] Parent email exists and is valid in database
- [ ] Check spam folder in Gmail

## If Still Not Working

1. **Check Render Environment Variables:**
   - Dashboard → Your Service → Environment → Check EMAIL_USERNAME and EMAIL_APP_PASSWORD

2. **Verify Gmail Settings:**
   - 2-Factor Authentication enabled
   - App Password generated (not regular password)
   - Less secure app access NOT needed (we use App Password)

3. **Test Locally:**
   Run the backend locally with correct credentials in application.properties and test the endpoints.

## Contact Information

If you need to regenerate Gmail App Password:
1. Google Account → Security → 2-Step Verification → App passwords
2. Generate new 16-character password
3. Update in Render environment variables
4. Restart Render service

---

**Note:** The homework alert runs at 11 PM. Use the manual trigger endpoint if you want to test it immediately without waiting.
