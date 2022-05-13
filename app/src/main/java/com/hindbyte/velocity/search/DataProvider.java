package com.hindbyte.velocity.search;

import android.annotation.SuppressLint;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class DataProvider {

    private final SearchAdapter searchAdapter;
    private SearchModel searchModel = null;
    private final List<SearchModel> searchModelList;

    public DataProvider(SearchAdapter searchAdapter, List<SearchModel> searchModelList) {
        this.searchAdapter = searchAdapter;
        this.searchModelList = searchModelList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void xmlParser(String response) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(response));
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        if (searchModelList.size() > 0) {
                            searchModelList.clear();
                            searchAdapter.notifyDataSetChanged();
                        }
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equalsIgnoreCase("CompleteSuggestion")) {
                            searchModel = new SearchModel();
                        }  else if(searchModel != null) {
                            if (parser.getName().equalsIgnoreCase("suggestion")) {
                                String title = parser.getAttributeValue(null, "data");
                                searchModel.setTitle(title);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("CompleteSuggestion".equalsIgnoreCase(parser.getName())) {
                            searchModelList.add(searchModel);
                            searchAdapter.notifyDataSetChanged();
                            searchModel = null;
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
