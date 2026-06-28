package test.java.controllers.GameController;

import controllers.GameController.NewsController;
import models.App;
import models.Result;
import models.news.Message;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NewsControllerUnitTest {

    private NewsController controller;

    @Before
    public void setUp() throws Exception {
        App.getNews().getMessages().clear();
        controller = new NewsController();
    }

    private void addMessages(Message... messages) {
        for (Message m : messages)
            App.getNews().addMessages(m);
    }


    @Test
    public void testShowUnreadNews_singleUnreadMessage_returnsMessageText() {
        addMessages(new Message("msg1"));
        Result result = controller.showUnreadNews();
        assertTrue(result.isSuccessful());
        assertEquals("msg1", result.message());
    }

    @Test
    public void testShowUnreadNews_multipleUnreadMessages_returnsAllTexts() {
        addMessages(new Message("msg1"), new Message("msg2"), new Message("msg3"));
        Result result = controller.showUnreadNews();
        assertTrue(result.isSuccessful());
        assertTrue(result.message().contains("msg1"));
        assertTrue(result.message().contains("msg2"));
        assertTrue(result.message().contains("msg3"));
    }

    @Test
    public void testShowUnreadNews_marksUnreadMessagesAsRead() {
        Message msg1 = new Message("msg1");
        Message msg2 = new Message("msg2");
        addMessages(msg1, msg2);

        controller.showUnreadNews();

        assertFalse(msg1.isUnread());
        assertFalse(msg2.isUnread());
    }

    @Test
    public void testShowUnreadNews_excludesAlreadyReadMessages() {
        Message readMsg = new Message("already read");
        readMsg.setUnread(false);
        Message unreadMsg = new Message("new message");
        addMessages(readMsg, unreadMsg);

        Result result = controller.showUnreadNews();
        assertFalse(result.message().contains("already read"));
        assertTrue(result.message().contains("new message"));
    }

    @Test
    public void testShowUnreadNews_noUnreadMessages_returnsEmptyText() {
        Message readMsg = new Message("already read");
        readMsg.setUnread(false);
        addMessages(readMsg);

        Result result = controller.showUnreadNews();
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testShowUnreadNews_noMessages_returnsEmptyText() {
        Result result = controller.showUnreadNews();
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testShowUnreadNews_calledTwice_secondCallReturnsEmpty() {
        addMessages(new Message("msg1"), new Message("msg2"));

        controller.showUnreadNews();
        Result secondResult = controller.showUnreadNews();

        assertTrue(secondResult.isSuccessful());
        assertEquals("", secondResult.message());
    }

    @Test
    public void testShowUnreadNews_mixedMessages_onlyUnreadInResult() {
        Message read1 = new Message("first read message");
        read1.setUnread(false);
        Message unread1 = new Message("first unread message");
        Message read2 = new Message("second read message");
        read2.setUnread(false);
        Message unread2 = new Message("second unread message");
        addMessages(read1, unread1, read2, unread2);

        Result result = controller.showUnreadNews();
        assertFalse(result.message().contains("first read message"));
        assertFalse(result.message().contains("second read message"));
        assertTrue(result.message().contains("first unread message"));
        assertTrue(result.message().contains("second unread message"));
    }

    @Test
    public void testShowUnreadNews_doesNotChangeReadStatusOfAlreadyReadMessages() {
        Message readMsg = new Message("already read");
        readMsg.setUnread(false);
        addMessages(readMsg);

        controller.showUnreadNews();
        assertFalse(readMsg.isUnread());
    }

    @Test
    public void testShowUnreadNews_multipleMessages_noTrailingNewline() {
        addMessages(new Message("msg1"), new Message("msg2"));
        Result result = controller.showUnreadNews();
        assertFalse(result.message().endsWith("\n"));
    }


    @Test
    public void testShowAllNews_singleMessage_returnsMessageText() {
        addMessages(new Message("msg1"));
        Result result = controller.showAllNews();
        assertTrue(result.isSuccessful());
        assertEquals("msg1", result.message());
    }

    @Test
    public void testShowAllNews_multipleMessages_returnsAllTexts() {
        addMessages(new Message("msg1"), new Message("msg2"), new Message("msg3"));
        Result result = controller.showAllNews();
        assertTrue(result.isSuccessful());
        assertTrue(result.message().contains("msg1"));
        assertTrue(result.message().contains("msg2"));
        assertTrue(result.message().contains("msg3"));
    }

    @Test
    public void testShowAllNews_marksAllMessagesAsRead() {
        Message msg1 = new Message("msg1");
        Message msg2 = new Message("msg2");
        addMessages(msg1, msg2);

        controller.showAllNews();

        assertFalse(msg1.isUnread());
        assertFalse(msg2.isUnread());
    }

    @Test
    public void testShowAllNews_includesAlreadyReadMessages() {
        Message readMsg = new Message("already read");
        readMsg.setUnread(false);
        addMessages(readMsg);

        Result result = controller.showAllNews();
        assertTrue(result.isSuccessful());
        assertTrue(result.message().contains("already read"));
    }

    @Test
    public void testShowAllNews_noMessages_returnsEmptyText() {
        Result result = controller.showAllNews();
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testShowAllNews_calledTwice_secondCallStillReturnsAllMessages() {
        addMessages(new Message("msg1"), new Message("msg2"));

        controller.showAllNews();
        Result secondResult = controller.showAllNews();

        assertTrue(secondResult.isSuccessful());
        assertTrue(secondResult.message().contains("msg1"));
        assertTrue(secondResult.message().contains("msg2"));
    }

    @Test
    public void testShowAllNews_mixedReadAndUnread_returnsAll() {
        Message readMsg = new Message("read");
        readMsg.setUnread(false);
        Message unreadMsg = new Message("unread");
        addMessages(readMsg, unreadMsg);

        Result result = controller.showAllNews();
        assertTrue(result.message().contains("read"));
        assertTrue(result.message().contains("unread"));
    }

    @Test
    public void testShowAllNews_multipleMessages_noTrailingNewline() {
        addMessages(new Message("msg1"), new Message("msg2"));
        Result result = controller.showAllNews();
        assertFalse(result.message().endsWith("\n"));
    }
}