package com.example.unitrade;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class SellFragment extends Fragment {

    // Inputs
    private EditText edtItemName, edtUsedDuration, edtPrice, edtLocation, edtDescription;
    private Spinner spinnerDurationUnit;

    private boolean fromExternal = false;

    // Category buttons
    private MaterialButton btnCatStationery, btnCatBook, btnCatPersonalCare,
            btnRoomEssentials, btnCatFashion, btnCatTextbook,
            btnCatSports, btnCatHobbies, btnCatFood, btnCatOthers;

    // Condition buttons
    private MaterialButton btnCondGood, btnCondFair, btnCondLikeNew,
            btnCondBrandNew, btnCondNotWorking,
            btnCondExcellent, btnCondOldButWorking, btnCondRefurbished;

    // Category more section
    private LinearLayout layoutCategoryMoreRow1, layoutCategoryMoreRow2;
    private Button btnShowMoreCategory, btnShowLessCategory;

    // Condition more section
    private LinearLayout layoutConditionMore;
    private Button btnShowMoreCondition, btnShowLessCondition;

    // Image upload
    private ViewPager2 viewPagerSellImages;
    private LinearLayout layoutImagePlaceholder;
    private TabLayout tabDotsSell;
    private Button btnUploadImages;

    private List<Uri> selectedImages = new ArrayList<>();
    private static final int PICK_IMAGES = 101;

    // Selected Values
    private String selectedCategory = null;
    private String selectedCondition = null;

    // Edit mode
    private boolean isEditMode = false;
    private Product productToEdit;


    public SellFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Receive arguments
        if (getArguments() != null) {
            fromExternal = getArguments().getBoolean("fromExternal", false);
            isEditMode = getArguments().getBoolean("editMode", false);
            productToEdit = getArguments().getParcelable("product_to_edit");
        }

        // Required to receive the toolbar back button click events
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sell, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupShowMoreLess();
        setupCategorySelection();
        setupConditionSelection();
        setupUnitSpinner();
        setupImageUpload();

        if (isEditMode && productToEdit != null) {
            fillEditForm();
        }

        setupPostButton();
    }


    // ======================================================
    // FIXED TOOLBAR LOGIC (BACK BUTTON WORKS NOW)
    // ======================================================

    @Override
    public void onResume() {
        super.onResume();

        super.onResume();

        AppCompatActivity act = (AppCompatActivity) requireActivity();

        if (act.getSupportActionBar() != null) {
            if (fromExternal) {
                act.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                act.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }


    }


    @Override
    public void onStop() {
        super.onStop();

        // Reset toolbar to avoid stuck back arrow when leaving
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getSupportActionBar().show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            if (fromExternal) {
                requireActivity().finish();
            } else {
                NavHostFragment.findNavController(this).navigateUp();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // ======================================================
    // Bind Views
    // ======================================================

    private void bindViews(View v) {

        edtItemName = v.findViewById(R.id.edtItemName);
        edtUsedDuration = v.findViewById(R.id.edtUsedDuration);
        edtPrice = v.findViewById(R.id.edtPrice);
        edtLocation = v.findViewById(R.id.edtLocation);
        edtDescription = v.findViewById(R.id.edtDescription);
        spinnerDurationUnit = v.findViewById(R.id.spinnerDurationUnit);

        btnCatStationery   = v.findViewById(R.id.btnCatStationery);
        btnCatBook         = v.findViewById(R.id.btnCatBook);
        btnCatPersonalCare = v.findViewById(R.id.btnCatPersonalCare);
        btnRoomEssentials  = v.findViewById(R.id.btnRoomEssentials);
        btnCatFashion      = v.findViewById(R.id.btnCatFashion);
        btnCatTextbook     = v.findViewById(R.id.btnCatTextbook);
        btnCatSports       = v.findViewById(R.id.btnCatSports);
        btnCatHobbies      = v.findViewById(R.id.btnCatHobbies);
        btnCatFood         = v.findViewById(R.id.btnCatFood);
        btnCatOthers       = v.findViewById(R.id.btnCatOthers);

        layoutCategoryMoreRow1 = v.findViewById(R.id.layoutCategoryMoreRow1);
        layoutCategoryMoreRow2 = v.findViewById(R.id.layoutCategoryMoreRow2);
        btnShowMoreCategory = v.findViewById(R.id.btnShowMoreCategory);
        btnShowLessCategory = v.findViewById(R.id.btnShowLessCategory);

        btnCondGood         = v.findViewById(R.id.btnCondGood);
        btnCondFair         = v.findViewById(R.id.btnCondFair);
        btnCondLikeNew      = v.findViewById(R.id.btnCondLikeNew);
        btnCondBrandNew     = v.findViewById(R.id.btnCondBrandNew);
        btnCondNotWorking   = v.findViewById(R.id.btnCondNotWorking);
        btnCondExcellent    = v.findViewById(R.id.btnCondExcellent);
        btnCondOldButWorking= v.findViewById(R.id.btnCondOldButWorking);
        btnCondRefurbished  = v.findViewById(R.id.btnCondRefurbished);

        layoutConditionMore = v.findViewById(R.id.layoutConditionMore);
        btnShowMoreCondition = v.findViewById(R.id.btnShowMoreCondition);
        btnShowLessCondition = v.findViewById(R.id.btnShowLessCondition);

        viewPagerSellImages = v.findViewById(R.id.viewPagerSellImages);
        layoutImagePlaceholder = v.findViewById(R.id.layoutImagePlaceholder);
        tabDotsSell = v.findViewById(R.id.tabDotsSell);
        btnUploadImages = v.findViewById(R.id.btnUploadImages);
    }


    // ======================================================
    // Category & Condition Section
    // ======================================================

    private void setupShowMoreLess() {

        btnShowMoreCategory.setOnClickListener(v -> {
            layoutCategoryMoreRow1.setVisibility(View.VISIBLE);
            layoutCategoryMoreRow2.setVisibility(View.VISIBLE);
            btnShowMoreCategory.setVisibility(View.GONE);
            btnShowLessCategory.setVisibility(View.VISIBLE);
        });

        btnShowLessCategory.setOnClickListener(v -> {
            layoutCategoryMoreRow1.setVisibility(View.GONE);
            layoutCategoryMoreRow2.setVisibility(View.GONE);
            btnShowMoreCategory.setVisibility(View.VISIBLE);
            btnShowLessCategory.setVisibility(View.GONE);
        });

        btnShowMoreCondition.setOnClickListener(v -> {
            layoutConditionMore.setVisibility(View.VISIBLE);
            btnShowMoreCondition.setVisibility(View.GONE);
            btnShowLessCondition.setVisibility(View.VISIBLE);
        });

        btnShowLessCondition.setOnClickListener(v -> {
            layoutConditionMore.setVisibility(View.GONE);
            btnShowMoreCondition.setVisibility(View.VISIBLE);
            btnShowLessCondition.setVisibility(View.GONE);
        });
    }


    private void setupCategorySelection() {

        List<MaterialButton> categoryButtons = List.of(
                btnCatStationery, btnCatBook, btnCatPersonalCare,
                btnRoomEssentials, btnCatFashion, btnCatTextbook,
                btnCatSports, btnCatHobbies, btnCatFood, btnCatOthers
        );

        for (MaterialButton b : categoryButtons) {
            b.setOnClickListener(v -> {
                for (MaterialButton x : categoryButtons)
                    x.setChecked(false);

                b.setChecked(true);
                selectedCategory = b.getText().toString();
            });
        }
    }


    private void setupConditionSelection() {

        List<MaterialButton> condButtons = List.of(
                btnCondGood, btnCondFair, btnCondLikeNew,
                btnCondBrandNew, btnCondNotWorking,
                btnCondExcellent, btnCondOldButWorking, btnCondRefurbished
        );

        for (MaterialButton b : condButtons) {
            b.setOnClickListener(v -> {
                for (MaterialButton x : condButtons)
                    x.setChecked(false);

                b.setChecked(true);
                selectedCondition = b.getText().toString();
            });
        }
    }


    // ======================================================
    // Image Upload
    // ======================================================

    private void setupImageUpload() {
        btnUploadImages.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, PICK_IMAGES);
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES &&
                resultCode == Activity.RESULT_OK &&
                data != null) {

            selectedImages.clear();

            // MULTIPLE
            if (data.getClipData() != null) {
                int count = Math.min(data.getClipData().getItemCount(), 5);
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    selectedImages.add(uri);
                }
            } else if (data.getData() != null) {
                selectedImages.add(data.getData());
            }

            showSelectedImages();
        }
    }


    private void showSelectedImages() {

        layoutImagePlaceholder.setVisibility(View.GONE);
        viewPagerSellImages.setVisibility(View.VISIBLE);

        SellImageSliderAdapter adapter =
                new SellImageSliderAdapter(getContext(), selectedImages);

        viewPagerSellImages.setAdapter(adapter);

        new TabLayoutMediator(tabDotsSell, viewPagerSellImages,
                (tab, pos) -> {}).attach();
    }


    // ======================================================
    // Spinner
    // ======================================================

    private void setupUnitSpinner() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_text_white,
                new String[]{"Months", "Years", "Days"}
        );

        adapter.setDropDownViewResource(R.layout.spinner_drop_down_chip);
        spinnerDurationUnit.setAdapter(adapter);
    }


    // ======================================================
    // Edit Mode
    // ======================================================

    private void fillEditForm() {

        edtItemName.setText(productToEdit.getName());
        edtPrice.setText(String.valueOf(productToEdit.getPrice()));
        edtLocation.setText(productToEdit.getLocation());
        edtDescription.setText(productToEdit.getDescription());

        int usedDays = productToEdit.getUsedDaysTotal();
        if (usedDays > 0) {
            edtUsedDuration.setText(String.valueOf(usedDays));

            int pos = ((ArrayAdapter<String>) spinnerDurationUnit.getAdapter())
                    .getPosition("Days");
            if (pos >= 0) spinnerDurationUnit.setSelection(pos);
        }

        selectCategoryButton(productToEdit.getCategory());
        selectConditionButton(productToEdit.getCondition());

        if (productToEdit.getImageUrls() != null && !productToEdit.getImageUrls().isEmpty()) {
            selectedImages.clear();
            for (String img : productToEdit.getImageUrls()) {
                selectedImages.add(Uri.parse(img));
            }
            showSelectedImages();
        }

        Button btn = requireView().findViewById(R.id.btnPostItem);
        btn.setText("Edit Item");
    }


    private void selectCategoryButton(String category) {

        List<MaterialButton> catBtns = List.of(
                btnCatStationery, btnCatBook, btnCatPersonalCare, btnRoomEssentials,
                btnCatFashion, btnCatTextbook, btnCatSports, btnCatHobbies,
                btnCatFood, btnCatOthers
        );

        for (MaterialButton b : catBtns) {
            if (b.getText().toString().equals(category)) {
                b.setChecked(true);
                selectedCategory = category;
            } else {
                b.setChecked(false);
            }
        }
    }


    private void selectConditionButton(String cond) {

        List<MaterialButton> condBtns = List.of(
                btnCondGood, btnCondFair, btnCondLikeNew, btnCondBrandNew,
                btnCondNotWorking, btnCondExcellent, btnCondOldButWorking, btnCondRefurbished
        );

        for (MaterialButton b : condBtns) {
            if (b.getText().toString().equals(cond)) {
                b.setChecked(true);
                selectedCondition = cond;
            } else {
                b.setChecked(false);
            }
        }
    }


    // ======================================================
    // Posting Logic
    // ======================================================

    private void setupPostButton() {
        Button btnPost = requireView().findViewById(R.id.btnPostItem);
        btnPost.setOnClickListener(v -> {
            if (isEditMode) {
                showEditConfirmDialog();
            } else {
                submitForm();
            }
        });
    }


    private void submitForm() {

        if (edtItemName.getText().toString().trim().isEmpty()) {
            toast("Please enter item name");
            return;
        }

        if (selectedCategory == null) {
            toast("Please select a category");
            return;
        }

        if (selectedCondition == null) {
            toast("Please select item condition");
            return;
        }

        if (edtPrice.getText().toString().trim().isEmpty()) {
            toast("Please enter a price");
            return;
        }

        if (edtLocation.getText().toString().trim().isEmpty()) {
            toast("Please enter item location");
            return;
        }

        toast("Item posted successfully!");
    }


    private void updateItem() {

        String name = edtItemName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();
        String usedDurationStr = edtUsedDuration.getText().toString().trim();

        if (name.isEmpty()) {
            toast("Please enter item name");
            return;
        }

        if (selectedCategory == null) {
            toast("Please select a category");
            return;
        }

        if (selectedCondition == null) {
            toast("Please select item condition");
            return;
        }

        if (priceStr.isEmpty()) {
            toast("Please enter a price");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            toast("Invalid price");
            return;
        }

        if (location.isEmpty()) {
            toast("Please enter item location");
            return;
        }

        productToEdit.setName(name);
        productToEdit.setDescription(desc);
        productToEdit.setPrice(price);
        productToEdit.setLocation(location);
        productToEdit.setCategory(selectedCategory);
        productToEdit.setCondition(selectedCondition);

        // Convert duration to days
        int usedDays = 0;
        if (!usedDurationStr.isEmpty()) {
            try {
                int value = Integer.parseInt(usedDurationStr);
                String unit = spinnerDurationUnit.getSelectedItem().toString();
                switch (unit) {
                    case "Years":  usedDays = value * 365; break;
                    case "Months": usedDays = value * 30; break;
                    case "Days":   usedDays = value;       break;
                }
            } catch (Exception e) {
                toast("Invalid duration");
                return;
            }
        }
        productToEdit.setUsedDaysTotal(usedDays);

        if (!selectedImages.isEmpty()) {
            List<String> newUrls = new ArrayList<>();
            for (Uri uri : selectedImages) newUrls.add(uri.toString());
            productToEdit.setImageUrls(newUrls);
        }

        toast("Item updated!");

        Intent result = new Intent();
        result.putExtra("updated_product", productToEdit);

        requireActivity().setResult(Activity.RESULT_OK, result);
        requireActivity().finish();
    }


    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }


    private void showEditConfirmDialog() {
        ConfirmDialog.show(
                requireContext(),
                "Confirm Edit",
                "Are you sure you want to save changes to this item?",
                "Edit",
                () -> updateItem()
        );
    }

}
