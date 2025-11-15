# UptimeRobot Setup Guide - Keep Render Service Awake

## Problem
Render's free tier puts your service to sleep after 15 minutes of inactivity. This stops your scheduled notification tasks from running.

## Solution
Use UptimeRobot (free) to ping your backend every 5 minutes, keeping it awake 24/7.

---

## Step-by-Step Setup

### 1. Create UptimeRobot Account

1. Go to **https://uptimerobot.com/**
2. Click **"Sign Up Free"** (top right)
3. Fill in your details:
   - Email address
   - Password
   - Full name
4. Click **"Sign Up"**
5. Check your email and verify your account
6. Log in to UptimeRobot dashboard

---

### 2. Add New Monitor

Once logged in:

1. **Click "+ Add New Monitor"** (big button in center or top-right)

2. **Fill in the form:**

```
Monitor Type: HTTP(s)
─────────────────────────────────────────────────────────
Friendly Name:        IntegrationProject Backend
URL (or IP):          https://integrationprojectbackend.onrender.com/api/health
Monitoring Interval:  Every 5 minutes
Monitor Timeout:      30 seconds
─────────────────────────────────────────────────────────
```

3. **Optional - Add Alert Contacts (Recommended):**
   - Click **"Add Alert Contact"**
   - Choose **"Email"**
   - Enter your email address
   - Verify the email
   - This notifies you if your service goes down

4. **Click "Create Monitor"** at the bottom

---

### 3. Verify Monitor is Working

**Method 1: Test the Endpoint Manually**

Open your browser and visit:
```
https://integrationprojectbackend.onrender.com/api/health
```

You should see:
```json
{
  "status": "UP",
  "timestamp": "2025-11-16T14:30:00",
  "service": "IntegrationProjectBackend",
  "message": "Service is running"
}
```

**Method 2: Check UptimeRobot Dashboard**

1. Go back to UptimeRobot dashboard
2. Your monitor should appear in the list
3. Initial status might show **"?"** or **"Paused"**
4. Wait 5 minutes for first ping
5. Status should change to **"Up"** with a green checkmark ✅
6. You'll see:
   - **Uptime %**: Should be close to 100%
   - **Response Time**: Usually 100-500ms
   - **Status**: Up (green)

---

### 4. Monitor Dashboard Overview

After setup, your dashboard shows:

| Column | Description |
|--------|-------------|
| **Status** | Up (green ✅) / Down (red ❌) |
| **Uptime** | Percentage of time service was available |
| **Response Time** | How fast your API responds (ms) |
| **Last Check** | Timestamp of most recent ping |

**Click on your monitor** to see:
- Detailed uptime statistics
- Response time graph
- Recent downtime events
- Alert logs

---

## How It Works

```
┌─────────────────┐
│  UptimeRobot    │
│  Every 5 min    │
└────────┬────────┘
         │
         │ HTTP GET Request
         ▼
┌─────────────────────────────────┐
│  Render Service                 │
│  /api/health endpoint           │
│  ┌───────────────────────────┐ │
│  │ Returns: { status: "UP" } │ │
│  └───────────────────────────┘ │
└─────────────────────────────────┘
         │
         │ Keeps service awake!
         ▼
┌─────────────────────────────────┐
│  Scheduled Tasks Keep Running   │
│  - 10 PM: Homework reminders    │
│  - 11 PM: Revision alerts       │
│  - Every hour: Quiz alerts      │
└─────────────────────────────────┘
```

---

## UptimeRobot Free Tier Limits

✅ **Included Free:**
- **50 monitors** (you only need 1)
- **5-minute check intervals** (perfect for our needs)
- **Alert via email**
- **2 months of logs**
- **Public status page**

❌ **Not Included (Paid Only):**
- 1-minute intervals (not needed)
- SMS alerts (email is enough)
- More log history

**Verdict:** Free tier is perfect for this use case! ✅

---

## Troubleshooting

### Problem: Monitor shows "Down"

**Cause 1: Service not deployed yet**
- Wait for Render deployment to complete
- Check Render dashboard → "Deployed" status

**Cause 2: Health endpoint not working**
- Test manually: `https://integrationprojectbackend.onrender.com/api/health`
- Check for typos in URL

**Cause 3: Service is sleeping**
- First ping after sleep takes ~30 seconds to wake up
- UptimeRobot might timeout on first attempt
- It will retry in 5 minutes and should succeed

**Solution:**
- Wait 10-15 minutes for monitor to stabilize
- If still down, check Render logs for errors

---

### Problem: Monitor alternates between Up/Down

**Cause:** Render free tier cold start is slow (~30 seconds)

**Solutions:**
1. Increase monitor timeout to **60 seconds**:
   - Edit Monitor → Advanced Settings → Monitor Timeout: 60
2. This gives service time to wake from sleep
3. After first ping, subsequent checks will be fast

---

### Problem: Service still sleeping despite UptimeRobot

**Check:**
1. Verify monitor is **not paused** (green play icon)
2. Check "Last Check" timestamp - should be within 5 minutes
3. View monitor logs to see if requests are actually being sent

**Render Logs:**
- Go to Render Dashboard → Your Service → Logs
- Search for: `/api/health`
- You should see GET requests every 5 minutes

---

## Alternative Health Endpoints

Your backend now has two health endpoints:

### 1. `/api/health` (Detailed - Recommended)
```
GET https://integrationprojectbackend.onrender.com/api/health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-11-16T14:30:00",
  "service": "IntegrationProjectBackend",
  "message": "Service is running"
}
```

### 2. `/api/ping` (Simple - Backup)
```
GET https://integrationprojectbackend.onrender.com/api/ping
```

**Response:**
```
pong
```

Use either one - UptimeRobot just needs a 200 OK response!

---

## Expected Behavior After Setup

**Before UptimeRobot:**
- Service sleeps after 15 min inactivity ❌
- Scheduled tasks don't run ❌
- Students/parents don't receive emails ❌

**After UptimeRobot:**
- Service stays awake 24/7 ✅
- Scheduled tasks run on time:
  - 10 PM: Student homework reminders ✅
  - 11 PM: Parent revision alerts ✅
  - Every hour: Quiz notifications ✅
- Emails sent automatically ✅

---

## Monitoring Your Monitor 😄

### Set Up Email Alerts (Optional but Recommended)

1. Go to **"My Settings"** in UptimeRobot
2. Click **"Add Alert Contact"**
3. Choose **Email**
4. Enter your email
5. Verify the confirmation email
6. Now you'll be notified if service goes down!

**Alert Scenarios:**
- Service down for 5+ minutes → Email sent
- Service back up → Recovery email sent
- You'll know immediately if something breaks

---

## Cost Breakdown

| Service | Plan | Cost | Purpose |
|---------|------|------|---------|
| **Render** | Free | $0 | Hosting backend |
| **UptimeRobot** | Free | $0 | Keep service awake |
| **Aiven MySQL** | Free | $0 | Database |
| **Gmail SMTP** | Free | $0 | Send emails |
| **TOTAL** | | **$0/month** 🎉 | Full production system! |

---

## Next Steps After Setup

1. ✅ Create UptimeRobot account
2. ✅ Add monitor with `/api/health` endpoint
3. ✅ Wait 10 minutes for monitor to stabilize
4. ✅ Verify "Up" status in dashboard
5. ✅ Add Gmail App Password to Render (from EMAIL_SETUP.md)
6. ✅ Test notification system at 10 PM
7. ✅ Check Render logs for scheduler activity
8. ✅ Celebrate working automated system! 🎊

---

## Questions?

**Q: Can I use a different monitoring service?**
A: Yes! Alternatives include:
- Pingdom (free tier available)
- StatusCake (free tier available)
- Freshping (free for 50 checks)
- cron-job.org (free cron service)

**Q: Will this cost me anything?**
A: No! UptimeRobot free tier is perfect for 1 monitor with 5-min intervals.

**Q: What happens if I hit the free tier limits?**
A: You won't! You only need 1 monitor, and free tier includes 50.

**Q: Can I monitor multiple services?**
A: Yes! You can add monitors for your frontend, database, etc. (up to 50 free)

**Q: How do I know if my scheduled tasks are running?**
A: Check Render logs for messages like:
```
🔔 [timestamp] Running homework reminder check...
✅ Homework reminder check completed
```

---

## Summary

You've now set up a **completely free** 24/7 monitoring system that:
- Keeps your Render backend awake
- Ensures scheduled tasks run on time
- Sends you alerts if service goes down
- Requires zero maintenance

**Total setup time:** ~5 minutes
**Monthly cost:** $0
**Uptime:** ~99.9%

Enjoy your automated notification system! 🚀
