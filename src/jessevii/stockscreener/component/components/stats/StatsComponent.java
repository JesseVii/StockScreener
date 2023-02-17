package jessevii.stockscreener.component.components.stats;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.component.components.settings.Setting;
import jessevii.stockscreener.objects.Stock;
import jessevii.stockscreener.utils.ComponentUtil;
import jessevii.stockscreener.utils.RenderUtil;
import jessevii.stockscreener.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;

public class StatsComponent extends Component {
    public String data, dataTicker;
    public Type dataType;

    public JTextField ticker;
    public static Setting insiderTrades = new Setting(Setting.Mode.BOOLEAN, "InsiderTrades", false);
    public static Setting companyNews = new Setting(Setting.Mode.BOOLEAN, "CompanyNews", false);
    public static Setting alphavantageApiKey = new Setting(Setting.Mode.TEXT, "AlphaVantageApiKey", "");
    public static Setting finnhubApiKey = new Setting(Setting.Mode.TEXT, "FinnhubApiKey", "");

    public StatsComponent() {
        super("Stats", true);
    }

    @Override
    public void onInit(String... params) {
        JTextField ticker = ComponentUtil.createTextField("", 5, 5, 150, 40, 1, 20, false);
        dontScroll.add(ticker);
        ComponentUtil.setTextFieldOnlyUppercase(ticker);
        this.ticker = ticker;
        this.panel.add(ticker);
        setTickerText(params);

        Object[][] settingCheckboxes =
                {{Type.INSIDER_TRADES, insiderTrades, 10, "https://finnhub.io/api/v1/stock/insider-transactions?symbol=tickerHere&token=" + finnhubApiKey.stringValue()},
                {Type.COMPANY_NEWS, companyNews, 8, "https://finnhub.io/api/v1/company-news?symbol=tickerHere&from=2021-09-01&to=2021-09-09&token=" + finnhubApiKey.stringValue()}};
        int index2 = 1;
        for (Object[] object : settingCheckboxes) {
            Setting setting = (Setting)object[1];

            String text = setting.name;
            int textWidth = ComponentUtil.getStringWidth(text, this.panel.getGraphics());
            JCheckBox candlesCheckbox = ComponentUtil.createCheckBox(text, this.panel.getWidth() - textWidth - 30, (this.panel.getHeight() - index2 * 18) - 10, textWidth + 25, 20, 13);
            final int index2Final = index2;
            onResize(() -> candlesCheckbox.setBounds(this.panel.getWidth() - textWidth - 30 - (int)object[2], (this.panel.getHeight() - index2Final * 18) - 10, textWidth + 50, 20));
            candlesCheckbox.setBackground(this.panel.getBackground());
            setting.addCheckBox(candlesCheckbox);
            candlesCheckbox.setSelected(setting.booleanValue());
            this.dontScroll.add(candlesCheckbox);

            candlesCheckbox.addActionListener(e -> {
                if (!setting.booleanValue()) {
                    for (Object[] object2 : settingCheckboxes) {
                        Setting setting2 = (Setting)object2[1];
                        if (!setting2.equals(setting)) {
                            setting2.checkBox.setSelected(false);
                            setting2.setValue(false);
                        }
                    }

                    this.panel.repaint();
                    this.dataType = (Type)object[0];
                    this.dataTicker = this.ticker.getText();
                    fetchData((String)object[3]);
                }
            });
            this.panel.add(candlesCheckbox);
            index2++;
        }
    }

    @Override
    public void onEnabled(String... params) {
        setTickerText(params);
    }

    public void fetchData(String request) {
        data = Utils.getHtml(request.replace("tickerHere", this.ticker.getText()));
    }

    public void setTickerText(String... params) {
        if (params != null && params.length > 0) {
            this.ticker.setText(params[0]);
        }
    }

    @Override
    public void onPaint(Graphics2D g) {
        if (dataType == Type.INSIDER_TRADES) {
            JSONArray jsonArray = new JSONObject(data).getJSONArray("data");
            int index = 0;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                double price = jsonObject.getDouble("transactionPrice");
                int change = jsonObject.getInt("change");

                RenderUtil.drawString(g, (change < 0 ? "-:227, 75, 90:-" : "-:75, 227, 95:-") + jsonObject.getString("name") + " (" + jsonObject.getString("symbol") + ")", 5, 5 + (index * 15), 15, true);
                index++;
                RenderUtil.drawString(g, RenderUtil.addColors("Change: " + change + " (" + Utils.formatToMBT((long)(jsonObject.getInt("change") * (price != 0 ? price : Stock.get(dataTicker, Integer.MAX_VALUE).getPrice()))) + ")"), 5, 5 + (index * 15), 15, true);
                index++;
                RenderUtil.drawString(g, RenderUtil.addColors("Shares held: " + jsonObject.getInt("share")), 5, 5 + (index * 15), 15, true);
                index++;
                RenderUtil.drawString(g, RenderUtil.addColors("Transaction Price: " + price), 5, 5 + (index * 15), 15, true);
                index++;
                RenderUtil.drawString(g, RenderUtil.addColors("Transaction Date: " + jsonObject.getString("transactionDate")), 5, 5 + (index * 15), 15, true);
                index++;
                RenderUtil.drawString(g, RenderUtil.addColors("Transaction Code: " + jsonObject.getString("transactionCode")), 5, 5 + (index * 15), 15, true);

                index += 2;
            }
        }
    }

    private enum Type {
        INSIDER_TRADES(),
        COMPANY_NEWS(),
    }
}
