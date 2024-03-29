package uw.playdesigner6;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by lybar_000 on 4/3/2015.
 */

public class MultiChoiceListDialogFragment extends DialogFragment {

    /*array list to save the indexes of the selected array items*/
    private ArrayList<Integer> selectedItemsIndexList;


    /*the interface to communicate with the host activity*/
    public interface multiChoiceListDialogListener {
        public void multipleChoiceOnOkay(ArrayList<Integer> arrayList);

        public void multipleChoiceOnCancel();

    }

    // Define listener variable
    multiChoiceListDialogListener dialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // ensure that the host activity implements the callback interface
        try {
            // Instantiate the dialogListener so we can send events to the host
            dialogListener = (multiChoiceListDialogListener) activity;
        } catch (ClassCastException e) {
            // if activity doesn't implement the interface, throw an exception
            throw new ClassCastException(activity.toString()
                    + " must implement multiChoiceListDialogListener");
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
        selectedItemsIndexList = new ArrayList();

        // Create logical array indicating whether or not the item is selected
        boolean[] isSelectedArray = new boolean[itemList.length];

        // Create new alert dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the dialog title
        builder.setTitle(R.string.titleSelectFileToPlay)

                // Specify the list array, multiple-choice list
                .setMultiChoiceItems(itemList, isSelectedArray,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item,
                                    // add it to the selected items list
                                    selectedItemsIndexList.add(which);
                                } else if (selectedItemsIndexList.contains(which)) {
                                    // Else, if the item is already in the list, remove it
                                    selectedItemsIndexList.remove(Integer.valueOf(which));
                                }
                            }
                        })

                // Set the action buttons
                .setPositiveButton(R.string.buttonOkay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so pass the selectedItemsIndexList
                        // results to the host activity
                        dialogListener.multipleChoiceOnOkay(selectedItemsIndexList);
                    }
                })

                .setNegativeButton(R.string.buttonCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialogListener.multipleChoiceOnCancel();
                    }
                });

        return builder.create();
    }
}
