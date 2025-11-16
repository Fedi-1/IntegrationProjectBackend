# 🚀 Quick Email Testing Reference

## ⚡ Fastest Way to Test (After Deployment)

### 1. Simple Test
Open browser and go to:
```
https://integrationprojectbackend.onrender.com/api/test-email?to=YOUR-EMAIL@gmail.com
```
Replace `YOUR-EMAIL` with your actual email.

### 2. Check Response
**Success:**
```json
{
  "success": true,
  "message": "Test email sent successfully!"
}
```

**Failed:**
```json
{
  "success": false,
  "error": "Authentication failed"
}
```

### 3. Check Your Email
- Look in **Inbox**
- Check **Spam/Junk** folder
- Subject: "✅ Test Email from IntegrationProject Backend"

---

## 📋 5 Ways to Verify Email is Working

| Method | Time | Difficulty | Reliability |
|--------|------|------------|-------------|
| **1. Test Endpoint** | 2 min | ⭐ Easy | ⭐⭐⭐⭐⭐ |
| **2. Render Logs** | 1 min | ⭐⭐ Medium | ⭐⭐⭐⭐⭐ |
| **3. Wait for 10 PM** | Hours | ⭐ Easy | ⭐⭐⭐⭐⭐ |
| **4. Local Testing** | 10 min | ⭐⭐⭐ Hard | ⭐⭐⭐⭐ |
| **5. Test Controller** | 15 min | ⭐⭐⭐⭐ Expert | ⭐⭐⭐⭐⭐ |

---

## 🔍 Check Render Logs

1. Go to: https://dashboard.render.com/
2. Click: **IntegrationProjectBackend**
3. Click: **Logs** (left sidebar)
4. Search for: `✅` (successful operations)

**What to look for:**
```
✅ Email sent to: your-email@gmail.com
✅ Reminder email sent to: student@example.com
✅ Sent reminder to John Doe (3 tasks)
```

**Errors show as:**
```
❌ Failed to send email to: email@example.com
```

---

## ⏰ Scheduled Task Times

| Task | Time (Server Time) | Log Message |
|------|-------------------|-------------|
| **Homework Reminders** | 10:00 PM daily | 🔔 Running homework reminder check... |
| **Revision Alerts** | 11:00 PM daily | 🔔 Running revision adherence check... |
| **Quiz Notifications** | Every hour | 🔔 Running quiz score check... |

**Note:** Server time is UTC. Tunisia is UTC+1, so:
- 10 PM server time = 11 PM Tunisia time
- 11 PM server time = 12 AM (midnight) Tunisia time

---

## 🚨 Common Issues & Quick Fixes

### Issue: "success: false"
**Fix:** Check Gmail App Password is set in Render environment variables
```
EMAIL_USERNAME = your-email@gmail.com
EMAIL_APP_PASSWORD = xxxx xxxx xxxx xxxx
```

### Issue: Email not received
**Fix:** Check spam folder! 90% of "missing" emails are there

### Issue: 404 on /api/test-email
**Fix:** Wait 5-10 minutes for Render to deploy new code

### Issue: No logs showing
**Fix:** Service might be sleeping - set up UptimeRobot

---

## ✅ Complete Testing Checklist

**Before Testing:**
- [ ] Code pushed to GitHub
- [ ] Render deployment completed (status: "Live")
- [ ] Gmail App Password created
- [ ] Environment variables set in Render
- [ ] UptimeRobot monitoring active

**Testing:**
- [ ] Test endpoint returns success
- [ ] Test email received in inbox
- [ ] Logs show "✅ Email sent"
- [ ] No errors in Render logs

**Production Verification:**
- [ ] Student/parent accounts have valid emails
- [ ] Generated schedule with incomplete tasks
- [ ] Waited until 10 PM - reminder received
- [ ] Quiz completed - parent notified

---

## 📞 Quick Support

**Problem:** Can't find logs
→ Render Dashboard → Your Service → Logs tab

**Problem:** Environment variables not working
→ Render Dashboard → Environment → Add/Edit → Save → Wait for redeploy

**Problem:** Still no emails after 24 hours
→ Read full guide: `EMAIL_TESTING_GUIDE.md`

---

## 🎯 Success Indicators

You'll know it's working when:
1. ✅ Test endpoint returns `"success": true`
2. ✅ You receive test email in inbox
3. ✅ Render logs show email activity
4. ✅ No error messages in logs
5. ✅ Scheduled tasks run at correct times

**When all 5 are checked, your system is fully operational!** 🎉

---

## 📚 Full Documentation

For detailed explanations and advanced testing:
- `EMAIL_SETUP.md` - Complete Gmail configuration guide
- `EMAIL_TESTING_GUIDE.md` - All testing methods explained
- `UPTIMEROBOT_SETUP.md` - Keep service awake 24/7

---

**Last Updated:** November 16, 2025
**Your Integration Project is ready to send automated notifications!** 🚀
