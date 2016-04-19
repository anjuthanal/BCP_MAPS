package com.bcp.bcp.beacon;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.bcp.bcp.MainActivity;
import com.bcp.bcp.R;

import java.util.ArrayList;

/**
 * Created by pkandagatla on 04/04/16.
 */
public class ImageCarouselDialog extends DialogFragment {

    Context mContext;

    TextView titleView, textView, closeDialog, okDialog;
    String title, text;
    ArrayList<String> imageUrls;

    ImageViewPager imagePager;

    public ImageCarouselDialog() {

    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    /**
     * Returns a new Instance of ImageCarouselDialog
     *
     * @param title Title of dialog (pass null to hide title)
     * @param text  Summary of dialog (pass null to hide summary)
     * @param url   ArrayList containing URLs of images (pass null to hide images)
     */

    public static ImageCarouselDialog newInstance(String title, String text, ArrayList<String> url) {
        ImageCarouselDialog imageCarouselDialog = new ImageCarouselDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("text", text);
        args.putStringArrayList("url", url);
        imageCarouselDialog.setArguments(args);
        return imageCarouselDialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.dialog_fragment_carousel_notification, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);

        mContext = getActivity();

        title = getArguments().getString("title");
        text = getArguments().getString("text");
        imageUrls = getArguments().getStringArrayList("url");

        closeDialog = (TextView) rootView.findViewById(R.id.dialog_close);
        titleView = (TextView) rootView.findViewById(R.id.carousel_title);
        textView = (TextView) rootView.findViewById(R.id.carousel_text);
        imagePager = (ImageViewPager) rootView.findViewById(R.id.image_pager);


        if (title != null && !title.isEmpty()) titleView.setText(title);
        else titleView.setVisibility(View.GONE);

        if (text != null && !text.isEmpty()) textView.setText(text);
        else textView.setVisibility(View.GONE);

        if (imageUrls != null && imageUrls.size() > 0) {
            imagePager.setVisibility(View.VISIBLE);
            PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());
            imagePager.setAdapter(pagerAdapter);
        }

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                MainActivity experienceDemoActivity = (MainActivity) getActivity();
                experienceDemoActivity.setIsPopupVisible(false);
                experienceDemoActivity.stopScanning();
            }
        });

        okDialog = (TextView) rootView.findViewById(R.id.dialog_ok);
        okDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity experienceDemoActivity = (MainActivity) getActivity();
                experienceDemoActivity.setIsPopupVisible(false);
                experienceDemoActivity.stopScanning();
                Uri uri = Uri.parse(text);

            }
        });
        return rootView;
    }

    public class PagerAdapter extends FragmentPagerAdapter {


        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return imageUrls.size();
        }

        @Override
        public Fragment getItem(int position) {
            return CarouselImageFragment.newInstance(imageUrls.get(position));
        }

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof MainActivity) {
            MainActivity experienceDemoActivity = (MainActivity) getActivity();
            experienceDemoActivity.setIsPopupVisible(false);
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        if (getActivity() instanceof MainActivity) {
            MainActivity experienceDemoActivity = (MainActivity) getActivity();
            experienceDemoActivity.setIsPopupVisible(false);
        }
    }

}