package me.odium.test;

public final class Statements {
    public static final String ClearMail = "DELETE FROM SM_Mail WHERE target_id='%s'";
    public static final String InboxCount = "SELECT COUNT(target_id) AS inboxtotal FROM SM_Mail WHERE target_id='%s'";
    public static final String SendMail = "INSERT INTO SM_Mail (sender_id, sender, target_id, target, date, message, isread, expiration) VALUES ('%s','%s','%s','%s',NOW(),'%s',0,NULL);";
    public static final String CheckMessageOwn = "SELECT COUNT(id) FROM SM_Mail WHERE id='%d' AND target_id='%s'";
    public static final String ReadMail = "SELECT *,DATE_FORMAT(date, '%%e/%%b/%%Y %%H:%%i') as fdate, DATE_FORMAT(expiration, '%%e/%%b/%%Y %%H:%%i') as fexpiration FROM SM_Mail WHERE id=%d";
    public static final String MarkRead = "UPDATE SM_Mail SET isread=1, expiration=DATE_ADD(NOW(), INTERVAL %s DAY) WHERE id=%d";
    public static final String Inbox = "SELECT *, DATE_FORMAT(date, '%%e/%%b/%%Y %%H:%%i') as fdate FROM SM_Mail WHERE target_id='%s'";
    public static final String Outbox = "SELECT *, DATE_FORMAT(date, '%%e/%%b/%%Y %%H:%%i') as fdate FROM SM_Mail WHERE sender_id='%s'";
    public static final String Mailboxes = "SELECT DISTINCT target FROM SM_Mail";
}
