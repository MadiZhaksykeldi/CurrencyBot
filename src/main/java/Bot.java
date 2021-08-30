import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Bot extends TelegramLongPollingBot {

    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());

        try {
            String currency = getCurrency(text);
            if (currency.equals("0.0")) {
                sendMessage.setText("Not valid currency");
            } else {
                sendMessage.setText(getCurrency(text));
            }
        } catch (IOException e) {
            sendMessage.setText("Error");
        }

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String getCurrency(String text) throws IOException {
        URL url = new URL("http://data.fixer.io/api/latest?access_key=06ed14d3c9b0d640eca23b38b888f89f&format=1");

        Scanner scanner = new Scanner((InputStream) url.getContent());
        String result = "";
        while (scanner.hasNext()) {
            result += scanner.nextLine();
        }

        JSONObject object = new JSONObject(result);
        double currency = 0.0;
        if (object.has(text + "_KZT"))
            currency = object.getDouble(text + "_KZT");
        return Double.toString(currency);
    }

    public void sendHelpOrStart(Message msg, Boolean start) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(msg.getChatId().toString());
        sendMessage.setReplyToMessageId(msg.getMessageId());
        if (!start) {
            sendMessage.setText("Please choose the currency.");
        } else {
            sendMessage.setText("Program successfully started");
        }
        try {
            setButtons(sendMessage);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void setButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRowList = new ArrayList<KeyboardRow>();
        KeyboardRow keyboardRow = new KeyboardRow();

        KeyboardButton button1 = new KeyboardButton("USD");
        KeyboardButton button2 = new KeyboardButton("EUR");
        KeyboardButton button3 = new KeyboardButton("RUB");

        keyboardRow.add(button1);
        keyboardRow.add(button2);
        keyboardRow.add(button3);

        keyboardRowList.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    @Override
    public String getBotUsername() {
        return "CurrencyTestKZBot";
    }

    @Override
    public String getBotToken() {
        return "1957488992:AAFRw2uVpDUQY9TVUYARvMd4U5UBpMwFsEo";
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update.getMessage().getText());
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            String text = message.getText();
            if ("/start".equals(text)) {
                sendHelpOrStart(message, true);
            } else {
                sendMsg(message, message.getText());
            }
        }
    }
}