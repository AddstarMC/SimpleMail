package me.odium.test;

public enum Statements {
  ClearMail("DELETE FROM SM_Mail WHERE target_id=?"),
  InboxCount("SELECT COUNT(target_id) AS inboxtotal FROM SM_Mail WHERE target_id=?"),
  InboxCountUnread(
      "SELECT COUNT(target_id) AS inboxtotal FROM SM_Mail WHERE target_id=? AND isread=0"),
  SendMail("INSERT INTO SM_Mail (sender_id, sender, target_id, target, date, message, isread,"
      + " expiration) VALUES (?,?,?,?,NOW(),?,0,NULL);"),
  CheckMessageOwn("SELECT COUNT(id) FROM SM_Mail WHERE id=? AND target_id=?"),
  ReadMail("SELECT *,DATE_FORMAT(date, '%e/%b/%Y %H:%i') as fdate, DATE_FORMAT(expiration,"
      + " '%e/%b/%Y %H:%i') as fexpiration FROM SM_Mail WHERE id=?"),
  MarkRead("UPDATE SM_Mail SET isread=1, expiration=DATE_ADD(NOW(), INTERVAL ? DAY) "
      + "WHERE id=?"),
  Inbox("SELECT *, DATE_FORMAT(date, '%e/%b/%Y %H:%i') as fdate FROM SM_Mail"
      + " WHERE target_id=?"),
  InboxConsole("SELECT *, DATE_FORMAT(date, '%e/%b/%Y %H:%i') as fdate FROM SM_Mail"
      + " WHERE target LIKE ?"),
  Outbox("SELECT *, DATE_FORMAT(date, '%e/%b/%Y %H:%i') as fdate FROM SM_Mail"
      + " WHERE sender_id=?"),
  OutboxConsole("SELECT *, DATE_FORMAT(date, '%e/%b/%Y %H:%i') as fdate FROM SM_Mail "
      + "WHERE sender LIKE ?"),
  Mailboxes("SELECT target, Count(*) AS Messages, Count(*) - Sum(isread) AS Unread "
      + "FROM SM_Mail WHERE target LIKE ? GROUP BY target ORDER BY target LIMIT ?"),
  Purge("DELETE FROM SM_Mail WHERE expiration IS NOT NULL AND expiration < NOW()"),
  Delete("DELETE FROM SM_Mail WHERE id=? and target_id=?");

  private final String mSql;

  Statements(String sql) {
    mSql = sql;
  }

  public String getSql() {
    return mSql;
  }
}
