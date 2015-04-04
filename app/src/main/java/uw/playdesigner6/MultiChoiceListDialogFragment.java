package uw.playdesigner6;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by lybar_000 on 4/3/2015.
 */

public class MultiChoiceListDialogFragment extends DialogFragment {

    /*array list to save the indexes of the selected array items*/
    private ArrayList<Integer> selectedItemsIndexList;


    public CharSequence listForDialogue[] = { "Tea", "Coffee", "Milk" };


/*    public static MultiChoiceListDialogFragment newInstance(String[] list){
        MultiChoiceListDialogFragment fragment = new MultiChoiceListDialogFragment();
        Bundle args  = new Bundle();
        //args.putCharSequence("list",list[]);
        args.putStringArray("List", list);
        fragment.setArguments(args);
        return fragment;

    }*/

    /*the interface to communicate with the host activity*/
    public interface multiChoiceListDialogListener {
        public void onOkay(ArrayList<Integer> arrayList);

        public void onCancel();

    }

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
//        String[] list = getArguments().getStringArray("list");
        //System.out.println( list );

        Bundle bundle = this.getArguments();
        String[] myValue = bundle.getStringArray("list");
        System.out.println(Arrays.toString(myValue));

        //saves list of selected items indexes
        selectedItemsIndexList = new ArrayList();

        boolean[] isSelectedArray = new boolean[listForDialogue.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.select_file)
                // Specify the list array
                .setMultiChoiceItems(listForDialogue, isSelectedArray,
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
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so pass the selectedItemsIndexList
                        // results to the host activity
                        dialogListener.onOkay(selectedItemsIndexList);
                    }
                })

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialogListener.onCancel();
                    }
                });

        return builder.create();
    }
}
