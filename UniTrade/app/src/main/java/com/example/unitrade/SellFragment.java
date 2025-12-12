package com.example.unitrade;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.*;

public class SellFragment extends Fragment {

    // Inputs
    private EditText edtItemName, edtUsedDuration, edtPrice, edtLocation, edtDescription;
    private AutoCompleteTextView dropdownDurationUnit;
    private TextInputLayout layoutDurationUnit;

    private boolean imagesModified = false;

    // Mode
    private boolean isEditMode = false;
    private Product productToEdit;
    private String origin = null;

    // Category buttons
    private MaterialButton btnCatStationery, btnCatElectronics, btnCatPersonalCare,
            btnRoomEssentials, btnCatFashion, btnCatTextbook,
            btnCatSports, btnCatHobbies, btnCatFood, btnCatOthers;

    // Condition buttons
    private MaterialButton btnCondGood, btnCondFair, btnCondLikeNew,
            btnCondBrandNew, btnCondNotWorking, btnCondExcellent,
            btnCondOldButWorking, btnCondRefurbished;

    // Show More / Less
    private LinearLayout layoutCategoryMoreRow1, layoutCategoryMoreRow2;
    private LinearLayout layoutConditionMore;
    private Button btnShowMoreCategory, btnShowLessCategory;
    private Button btnShowMoreCondition, btnShowLessCondition;

    // Images
    private ViewPager2 viewPagerSellImages;
    private LinearLayout layoutImagePlaceholder;
    private TabLayout tabDotsSell;
    private Button btnUploadImages;
    private final List<String> selectedImages = new ArrayList<>();
    private static final int PICK_IMAGES = 101;

    // QR
    private LinearLayout layoutQRPlaceholder;
    private ImageView imgQRPreview;
    private Button btnUploadQR;
    private ImageButton btnRemoveQR;
    private Uri qrPaymentUri = null;
    private static final int PICK_QR = 102;

    // Selected fields
    private String selectedCategory = null;
    private String selectedCondition = null;


    public SellFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            isEditMode = getArguments().getBoolean("editMode", false);
            origin = getArguments().getString("origin");

            if (isEditMode) {
                String productId = getArguments().getString("product_id");

                if (productId != null) {
                    productToEdit = SampleData.getProductById(requireContext(), productId);
                }
            }
        }

        setHasOptionsMenu(true);
    }




    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_sell, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        bindViews(view);
        setupShowMoreLess();
        setupCategorySelection();
        setupConditionSelection();
        setupUnitSpinner();
        setupImageUpload();
        setupQRUpload();

        if (isEditMode && productToEdit != null)
            fillEditForm();

        setupPostButton();
    }

    // BIND VIEWS ------------------------------------------------------------
    private void bindViews(View v) {
        edtItemName = v.findViewById(R.id.edtItemName);
        edtUsedDuration = v.findViewById(R.id.edtUsedDuration);
        edtPrice = v.findViewById(R.id.edtPrice);
        edtLocation = v.findViewById(R.id.edtLocation);
        edtDescription = v.findViewById(R.id.edtDescription);

        dropdownDurationUnit = v.findViewById(R.id.dropdownDurationUnit);
        layoutDurationUnit = v.findViewById(R.id.layoutDurationUnit);

        btnCatStationery = v.findViewById(R.id.btnCatStationery);
        btnCatElectronics = v.findViewById(R.id.btnCatElectronics);
        btnCatPersonalCare = v.findViewById(R.id.btnCatPersonalCare);
        btnRoomEssentials = v.findViewById(R.id.btnRoomEssentials);
        btnCatFashion = v.findViewById(R.id.btnCatFashion);
        btnCatTextbook = v.findViewById(R.id.btnCatTextbook);
        btnCatSports = v.findViewById(R.id.btnCatSports);
        btnCatHobbies = v.findViewById(R.id.btnCatHobbies);
        btnCatFood = v.findViewById(R.id.btnCatFood);
        btnCatOthers = v.findViewById(R.id.btnCatOthers);

        layoutCategoryMoreRow1 = v.findViewById(R.id.layoutCategoryMoreRow1);
        layoutCategoryMoreRow2 = v.findViewById(R.id.layoutCategoryMoreRow2);
        btnShowMoreCategory = v.findViewById(R.id.btnShowMoreCategory);
        btnShowLessCategory = v.findViewById(R.id.btnShowLessCategory);

        btnCondGood = v.findViewById(R.id.btnCondGood);
        btnCondFair = v.findViewById(R.id.btnCondFair);
        btnCondLikeNew = v.findViewById(R.id.btnCondLikeNew);
        btnCondBrandNew = v.findViewById(R.id.btnCondBrandNew);
        btnCondNotWorking = v.findViewById(R.id.btnCondNotWorking);
        btnCondExcellent = v.findViewById(R.id.btnCondExcellent);
        btnCondOldButWorking = v.findViewById(R.id.btnCondOldButWorking);
        btnCondRefurbished = v.findViewById(R.id.btnCondRefurbished);

        layoutConditionMore = v.findViewById(R.id.layoutConditionMore);
        btnShowMoreCondition = v.findViewById(R.id.btnShowMoreCondition);
        btnShowLessCondition = v.findViewById(R.id.btnShowLessCondition);

        viewPagerSellImages = v.findViewById(R.id.viewPagerSellImages);
        layoutImagePlaceholder = v.findViewById(R.id.layoutImagePlaceholder);
        tabDotsSell = v.findViewById(R.id.tabDotsSell);
        btnUploadImages = v.findViewById(R.id.btnUploadImages);

        layoutQRPlaceholder = v.findViewById(R.id.layoutQRPlaceholder);
        imgQRPreview = v.findViewById(R.id.imgQRPreview);
        btnUploadQR = v.findViewById(R.id.btnUploadQR);
        btnRemoveQR = v.findViewById(R.id.btnRemoveQR);
    }

    // SHOW MORE / LESS ------------------------------------------------------
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

    // SPINNER ---------------------------------------------------------------
    private void setupUnitSpinner() {
        String[] units = {"Days", "Months", "Years"};
        dropdownDurationUnit.setAdapter(
                new ArrayAdapter<>(requireContext(), R.layout.item_dropdown, units)
        );
    }

    // IMAGES ---------------------------------------------------------------
    private void setupImageUpload() {
        btnUploadImages.setOnClickListener(v -> {

            if (selectedImages.size() >= 5) {
                toast("Maximum 5 Images Allowed");
                updateImageUploadButtonUI();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            );
            startActivityForResult(intent, PICK_IMAGES);

        });

        updateImageUploadButtonUI();
    }

    private void updateImageUploadButtonUI() {
        int count = selectedImages.size();

        if (count == 0) {
            btnUploadImages.setText("Upload Images");
            btnUploadImages.setEnabled(true);
        } else if (count < 5) {
            btnUploadImages.setText("Add Image");
            btnUploadImages.setEnabled(true);
        } else {
            btnUploadImages.setText("Maximum 5 Images");
            btnUploadImages.setEnabled(false);
        }
    }

    // QR UPLOAD -------------------------------------------------------------
    private void setupQRUpload() {
        btnUploadQR.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_QR);
        });

        btnRemoveQR.setOnClickListener(v -> {
            qrPaymentUri = null;
            imgQRPreview.setImageDrawable(null);
            layoutQRPlaceholder.setVisibility(View.VISIBLE);
            imgQRPreview.setVisibility(View.GONE);
            btnRemoveQR.setVisibility(View.GONE);
        });
    }

    // ACTIVITY RESULT -------------------------------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null) return;

        if (requestCode == PICK_IMAGES && resultCode == Activity.RESULT_OK && data != null) {


            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;

            if (data.getClipData() != null) {

                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();

                    requireContext()
                            .getContentResolver()
                            .takePersistableUriPermission(uri, takeFlags);

                    selectedImages.add(uri.toString());
                }

            } else if (data.getData() != null) {

                Uri uri = data.getData();

                requireContext()
                        .getContentResolver()
                        .takePersistableUriPermission(uri, takeFlags);

                selectedImages.add(uri.toString());
            }


            imagesModified = true;
            showSelectedImages();
            updateImageUploadButtonUI();
        }

        else if (requestCode == PICK_QR) {
            qrPaymentUri = data.getData();
            if (qrPaymentUri != null) {
                imgQRPreview.setVisibility(View.VISIBLE);
                layoutQRPlaceholder.setVisibility(View.GONE);
                imgQRPreview.setImageURI(qrPaymentUri);
                btnRemoveQR.setVisibility(View.VISIBLE);
            }
        }
    }

    // IMAGE VIEWPAGER -------------------------------------------------------
    private SellImageSliderAdapter sellImageAdapter;

    private void showSelectedImages() {

        List<String> imagesToDisplay = new ArrayList<>();

        if (selectedImages.isEmpty()) {
            // 🔥 Force ONE placeholder image
            imagesToDisplay.add(""); // empty string triggers placeholder
        } else {
            imagesToDisplay.addAll(selectedImages);
        }

        layoutImagePlaceholder.setVisibility(View.GONE);
        viewPagerSellImages.setVisibility(View.VISIBLE);

        sellImageAdapter =
                new SellImageSliderAdapter(
                        requireContext(),
                        imagesToDisplay,
                        () -> {
                            imagesModified = true;

                            // 🔥 sync adapter → fragment
                            selectedImages.clear();
                            selectedImages.addAll(sellImageAdapter.getImages());

                            updateImageUploadButtonUI();
                        }
                );

        viewPagerSellImages.setAdapter(sellImageAdapter);

        new TabLayoutMediator(tabDotsSell, viewPagerSellImages, (t, p) -> {})
                .attach();
    }


    // FILL EDIT FORM --------------------------------------------------------
    private void fillEditForm() {
        if (productToEdit == null) return;

        edtItemName.setText(productToEdit.getName());
        edtPrice.setText(String.valueOf(productToEdit.getPrice()));
        edtLocation.setText(productToEdit.getLocation());
        edtDescription.setText(productToEdit.getDescription());

        edtUsedDuration.setText(String.valueOf(productToEdit.getUsedDaysTotal()));
        dropdownDurationUnit.setText("Days", false);

        selectCategoryButton(productToEdit.getCategory());
        selectConditionButton(productToEdit.getCondition());

        selectedImages.clear();
        if (productToEdit.getImageUrls() != null) {
            selectedImages.addAll(productToEdit.getImageUrls()); // ✅ keep String
        }

        showSelectedImages();
        updateImageUploadButtonUI();

        if (productToEdit.getQrPaymentUrl() != null) {
            qrPaymentUri = Uri.parse(productToEdit.getQrPaymentUrl());
            imgQRPreview.setImageURI(qrPaymentUri);
            imgQRPreview.setVisibility(View.VISIBLE);
            layoutQRPlaceholder.setVisibility(View.GONE);
            btnRemoveQR.setVisibility(View.VISIBLE);
        }

        Button btn = requireView().findViewById(R.id.btnPostItem);
        btn.setText("Save Changes");
    }


    // CATEGORY SELECTION ----------------------------------------------------
    private void setupCategorySelection() {
        List<MaterialButton> list = Arrays.asList(
                btnCatStationery, btnCatElectronics, btnCatPersonalCare,
                btnRoomEssentials, btnCatFashion, btnCatTextbook,
                btnCatSports, btnCatHobbies, btnCatFood, btnCatOthers
        );

        for (MaterialButton b : list) {
            b.setOnClickListener(v -> {
                for (MaterialButton x : list) x.setChecked(false);
                b.setChecked(true);
                selectedCategory = b.getText().toString();
            });
        }
    }

    private void selectCategoryButton(String category) {
        List<MaterialButton> list = Arrays.asList(
                btnCatStationery, btnCatElectronics, btnCatPersonalCare,
                btnRoomEssentials, btnCatFashion, btnCatTextbook,
                btnCatSports, btnCatHobbies, btnCatFood, btnCatOthers
        );

        for (MaterialButton b : list) {
            if (b.getText().toString().equals(category)) {
                b.setChecked(true);
                selectedCategory = category;
            } else {
                b.setChecked(false);
            }
        }
    }

    // CONDITION SELECTION ---------------------------------------------------
    private void setupConditionSelection() {
        List<MaterialButton> list = Arrays.asList(
                btnCondGood, btnCondFair, btnCondLikeNew,
                btnCondBrandNew, btnCondNotWorking,
                btnCondExcellent, btnCondOldButWorking,
                btnCondRefurbished
        );

        for (MaterialButton b : list) {
            b.setOnClickListener(v -> {
                for (MaterialButton x : list) x.setChecked(false);
                b.setChecked(true);
                selectedCondition = b.getText().toString();
            });
        }
    }

    private void selectConditionButton(String condition) {
        List<MaterialButton> list = Arrays.asList(
                btnCondGood, btnCondFair, btnCondLikeNew,
                btnCondBrandNew, btnCondNotWorking,
                btnCondExcellent, btnCondOldButWorking,
                btnCondRefurbished
        );

        for (MaterialButton b : list) {
            if (b.getText().toString().equals(condition)) {
                b.setChecked(true);
                selectedCondition = condition;
            } else {
                b.setChecked(false);
            }
        }
    }

    // POST BUTTON -----------------------------------------------------------
    private void setupPostButton() {
        Button btnPost = requireView().findViewById(R.id.btnPostItem);

        btnPost.setOnClickListener(v -> {
            if (isEditMode) updateItem();
            else submitForm();
        });
    }

    // CREATE PRODUCT --------------------------------------------------------
    private void submitForm() {

        String id = "p" + UUID.randomUUID().toString().substring(0, 8);

        String name = edtItemName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();
        String usedStr = edtUsedDuration.getText().toString();

        if (name.isEmpty()) { toast("Enter item name"); return; }
        if (selectedCategory == null) { toast("Select category"); return; }
        if (selectedCondition == null) { toast("Select condition"); return; }
        if (priceStr.isEmpty()) { toast("Enter price"); return; }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (Exception e) {
            toast("Invalid price");
            return;
        }

        if (price > 0 && qrPaymentUri == null) {
            toast("QR required for paid items");
            return;
        }

        int usedDays = usedStr.isEmpty() ? 0 : Integer.parseInt(usedStr);

        // 🔥 CRITICAL FIX — CONVERT TO STRING PROPERLY
        List<String> images = new ArrayList<>();
        for (Object obj : selectedImages) {
            images.add(obj.toString());
        }

        String qrUrl = qrPaymentUri != null ? qrPaymentUri.toString() : null;

        User user = UserSession.get();
        String sellerId = user != null ? user.getId() : "u1";

        Product p = new Product(
                id, name, price, images, desc,
                selectedCondition, usedDays,
                "Available", selectedCategory, location,
                sellerId, qrUrl
        );

        // 🔥 REQUIRED FOR CACHE INVALIDATION
        p.setImageVersion(System.currentTimeMillis());

        SampleData.addProduct(requireContext(), p);

        toast("Item posted!");
        NavHostFragment.findNavController(this).popBackStack();
    }


    // UPDATE ITEM ----------------------------------------------------------
    private void updateItem() {

        if (productToEdit == null) {
            toast("Product not found");
            return;
        }

        String name = edtItemName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();
        String usedStr = edtUsedDuration.getText().toString().trim();

        if (name.isEmpty()) { toast("Enter item name"); return; }
        if (selectedCategory == null) { toast("Select category"); return; }
        if (selectedCondition == null) { toast("Select condition"); return; }
        if (priceStr.isEmpty()) { toast("Enter price"); return; }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (Exception e) {
            toast("Invalid price");
            return;
        }

        if (price > 0 && qrPaymentUri == null && productToEdit.getQrPaymentUrl() == null) {
            toast("QR required for paid items");
            return;
        }

        int usedDays = usedStr.isEmpty() ? 0 : Integer.parseInt(usedStr);

        // ----------------------------
        // UPDATE FIELDS
        // ----------------------------
        productToEdit.setName(name);
        productToEdit.setDescription(desc);
        productToEdit.setPrice(price);
        productToEdit.setLocation(location);
        productToEdit.setCategory(selectedCategory);
        productToEdit.setCondition(selectedCondition);
        productToEdit.setUsedDaysTotal(usedDays);

        // ----------------------------
        // 🔥 IMAGES — PROPER CONVERSION
        // ----------------------------
        if (imagesModified) {
            List<String> newImages = new ArrayList<>();
            for (Object obj : selectedImages) {
                newImages.add(obj.toString()); // ✅ FIX
            }

            productToEdit.setImageUrls(newImages);

            // 🔥 force refresh everywhere
            productToEdit.setImageVersion(System.currentTimeMillis());
        }

        // ----------------------------
        // QR PAYMENT
        // ----------------------------
        if (qrPaymentUri != null) {
            productToEdit.setQrPaymentUrl(qrPaymentUri.toString());
        }

        // ----------------------------
        // SAVE
        // ----------------------------
        SampleData.updateProduct(productToEdit);

        toast("Item updated!");
        navigateBackAfterSave();
    }

    private void navigateBackAfterSave() {

        if (origin != null) {
            requireActivity().finish();   // came from Activity
        } else {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }











    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
