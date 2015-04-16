package uw.playdesigner6;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.ArrayList;
/**
 *
 * Created by lybar_000 on 4/10/2015.
 */
public class SingleChoiceListDialogFragment extends DialogFragment {

    /*array list to save the indexes of the selected array items*/
    public int selectedItemIndex;



    /*the interface to communicate with the host activity*/
    public interface singleChoiceListDialogListener {
        public void singleChoiceOnOkay(int selectedItemIndex);

        public void singleChoiceOnCancel();

    }

    // Define listener variable
    singleChoiceListDialogListener dialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // ensure that the host activity implements the callback interface
        try {
            // Instantiate the dialogListener so we can send events to the host
            dialogListener = (singleChoiceListDialogListener) activity;
        } catch (ClassCastException e) {
            // if activity doesn't implement the interface, throw an exception
            throw new ClassCastException(activity.toString()
                    + " must implement singleChoiceListDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create bundle of arguments
        Bundle bundle = this.getArguments();

        // Add selection list to bundle of arguments
        String[] list = bundle.getStringArray("list");

        // Convert list as a string array to character sequence
        CharSequence[] itemList = list;

        // Create repository for list of selected items indexes
//         selectedItemsIndex = new int;

        // Create logical array indicating whether or not the item is selected
        boolean[] isSelectedArray = new boolean[itemList.length];
        int checkedItem;

        // Create new alert dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the dialog title
        builder.setTitle(R.string.titleSelectFileToPlay)

                // Specify the list array, multiple-choice list
                //.setMultiChoiceItems(itemList, isSelectedArray,
                //        new DialogInterface.OnMultiChoiceClickListener() {
                .setSingleChoiceItems(itemList, 0,
                         new DialogInterface.OnClickListener() {
                             @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /*if (isChecked) {
                                    // If the user checked the item,
                                    // add it to the selected items list
                                    selectedItemsIndexList.add(which);
                                } else if (selectedItemsIndexList.contains(which)) {
                                    // Else, if the item is already in the list, remove it
                                    selectedItemsIndexList.remove(Integer.valueOf(which));
                                }*/
                                 selectedItemIndex = which;
                                 System.out.println(which);


                            }
                        })

                        // Set the action buttons
                .setPositiveButton(R.string.buttonOkay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so pass the selectedItemsIndexList
                        // results to the host activity
                        dialogListener.singleChoiceOnOkay(selectedItemIndex);

                    }
                })

                .setNegativeButton(R.string.buttonCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialogListener.singleChoiceOnCancel();
                    }
                });

        return builder.create();
    }
}
