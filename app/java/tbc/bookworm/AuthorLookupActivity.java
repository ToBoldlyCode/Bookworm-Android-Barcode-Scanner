package tbc.bookworm;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.String;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import android.graphics.drawable.Drawable;
import android.webkit.WebView;

import org.xml.sax.InputSource;

public class AuthorLookupActivity extends AppCompatActivity {
    private TextView statusMessage;
    private TextView title;
    private WebView image;
    private String barcode;
    private String searchQuery;
    private Integer MAX = 5;
    private String[][] bookList = new String[MAX][2];
    private String API_KEY = BuildConfig.API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);

        statusMessage = (TextView) findViewById(R.id.status_message);
        title = (TextView) findViewById(R.id.title);
        image = (WebView) findViewById(R.id.webView);
        Intent intent = getIntent();
        searchQuery = intent.getStringExtra("id");
        new findMatch().execute();
    }

    private class findMatch extends AsyncTask<String, String, String[][]> {
        protected String[][] doInBackground(String... params) {
            String id = "";
            String query = "https://www.goodreads.com/author/list/" + searchQuery + "?format=xml&key=" + API_KEY;

            try {
                URL url = new URL(query);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                con.connect();
                int response = con.getResponseCode();

                InputStream in = con.getInputStream();
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String read;

                while ((read = br.readLine()) != null) {
                    sb.append(read);
                }

                br.close();
                Log.d("error", sb.toString());
                String xml = sb.toString();

                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputSource src = new InputSource();
                src.setCharacterStream(new StringReader(xml));

                Document doc = builder.parse(src);

                org.w3c.dom.Element bookNode = (org.w3c.dom.Element) doc.getElementsByTagName("books").item(0);
                String numBooks = bookNode.getAttribute("total");
                Log.d("error", numBooks);

                if (Integer.parseInt(numBooks) < 3) {
                    MAX = Integer.parseInt(numBooks);
                }

                for (int i = 0; i < MAX; i++) {
                    String book = doc.getElementsByTagName("title").item(i).getTextContent();
                    String image = doc.getElementsByTagName("image_url").item(2 * i).getTextContent();
                    bookList[i][0] = book;
                    bookList[i][1] = image;
                }

            } catch (IOException | ParserConfigurationException | SAXException e) {
                //do something
            }

            return bookList;
        }

        protected void onPostExecute(String[][] result) {
            statusMessage.setText("");
            String page = "<body style='background-color:#303030'>" +
                    "<table><tbody>";

            for (int i = 0; i < MAX; i++) {
                page += "<tr><td><img style ='vertical-align:middle' src=" + result[i][1] + "><td><font style='color:white'>" + result[i][0] + "</tr>";
            }

            page += "</tbody></table>";

            image.loadData(page, "text/html", "utf-8");
        }
    }
}