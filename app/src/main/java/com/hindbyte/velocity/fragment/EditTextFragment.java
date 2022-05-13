package com.hindbyte.velocity.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hindbyte.velocity.R;
import com.hindbyte.velocity.search.DataProvider;
import com.hindbyte.velocity.search.SearchAdapter;
import com.hindbyte.velocity.search.SearchModel;
import com.hindbyte.velocity.util.AsyncThread;
import com.hindbyte.velocity.util.URLUtils;
import com.hindbyte.velocity.util.WebRequest;

import java.util.ArrayList;
import java.util.List;

public class EditTextFragment extends Fragment implements SearchAdapter.ItemClickListener {

    private RelativeLayout mainInputBox;
    private AutoCompleteTextView inputBox;
    ImageView searchIcon;
    private SearchAdapter searchAdapter;
    private final List<SearchModel> searchModelList = new ArrayList<>();
    String searchText;

    BrowserInterface browserInterface;

    public EditTextFragment(String inputBoxUrl, BrowserInterface browserInterface) {
        this.searchText = inputBoxUrl;
        this.browserInterface = browserInterface;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View fragmentView = inflater.inflate(R.layout.edit_text_fragment, null);
        mainInputBox = fragmentView.findViewById(R.id.main_inputbox);
        inputBox = fragmentView.findViewById(R.id.main_inputbox_input);
        searchIcon = fragmentView.findViewById(R.id.search_icon);
        updateInputBox(searchText);
        showSoftInput(inputBox);
        searchAdapter = new SearchAdapter(searchModelList);
        RecyclerView recyclerView = fragmentView.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), 0));
        searchAdapter.setClickListener(this);
        recyclerView.setAdapter(searchAdapter);
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainInputBox.setOnClickListener(v -> {
            hideSoftInput(inputBox);
            onBackPress();
        });

        inputBox.setOnEditorActionListener((v, actionId, event) -> {
            hideSoftInput(inputBox);
            String query = inputBox.getText().toString().trim();
            browserInterface.updateTab(query);
            onBackPress();
            return true;
        });

        inputBox.addTextChangedListener(new TextWatcher() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void afterTextChanged(Editable s) {
                if (!searchText.equals(s.toString().trim())) {
                    searchText = s.toString().trim();
                    if (searchText.length() > 0) {
                        FetchSearchData fetchSearchData = new FetchSearchData();
                        fetchSearchData.execute("http://suggestqueries.google.com/complete/search?output=toolbar&hl=en&q=" + searchText.replace(" ","+"));
                    } else {
                        searchModelList.clear();
                        searchAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        searchIcon.setOnClickListener(v -> {
            hideSoftInput(inputBox);
            String query = inputBox.getText().toString().trim();
            browserInterface.updateTab(query);
            onBackPress();
        });
    }

    private class FetchSearchData extends AsyncThread {

        @Override
        protected void onPreExecute() {}

        @Override
        protected Object doInBackground(Object... params) {
            WebRequest webRequest = new WebRequest();
            return webRequest.makeWebServiceCall((String) params[0], webRequest.GET);
        }

        @Override
        protected void onPostExecute(Object result) {
            DataProvider dataProvider = new DataProvider(searchAdapter, searchModelList);
            dataProvider.xmlParser((String) result);
        }

    }


    private void showSoftInput(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideSoftInput(View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void updateInputBox(String query) {
        if (query != null) {
            URLUtils urlUtils = new URLUtils();
            inputBox.setText(Html.fromHtml(urlUtils.urlWrapper(query)), EditText.BufferType.SPANNABLE);
        } else {
            inputBox.setText(null);
        }
    }

    public void onBackPress() {
        FragmentTransaction fragmentTransaction;
        fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(this).commit();
    }

    @Override
    public void onItemClick(String searchText) {
        hideSoftInput(inputBox);
        browserInterface.updateTab(searchText);
        onBackPress();
    }

    @Override
    public void onNextClick(String searchText) {
        inputBox.setText(searchText);
        inputBox.setSelection(inputBox.getText().length());
    }
}