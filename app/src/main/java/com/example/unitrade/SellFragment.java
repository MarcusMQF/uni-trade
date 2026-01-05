package com.example.unitrade;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public SellFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        if (getArguments() != null) {
            isEditMode = getArguments().getBoolean("editMode", false);
            origin = getArguments().getString("origin");

            if (isEditMode) {
                String productId = getArguments().getString("product_id");
                if (productId != null) {
                    // Load from Firebase instead of SampleData
                    loadProductFromFirebase(productId);
                }
            }
        }

        setHasOptionsMenu(true);
    }

    // ADD THIS NEW METHOD
    private void loadProductFromFirebase(String productId) {
        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        productToEdit = documentSnapshot.toObject(Product.class);
                        if (productToEdit != null && getView() != null) {
                            fillEditForm();
                        }
                    } else {
                        toast("Product not found");
                        NavHostFragment.findNavController(this).popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SellFragment", "Error loading product", e);
                    toast("Failed to load product");
                    NavHostFragment.findNavController(this).popBackStack();
                });
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
        setupPostButton();

        // Fill form if in edit mode and product is already loaded
        if (isEditMode && productToEdit != null) {
            fillEditForm();
        }
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

        if (requestCode == PICK_IMAGES && data != null) {

            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;

            if (data.getClipData() != null) {

                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    selectedImages.add(uri.toString());
                }

            } else if (data.getData() != null) {

                Uri uri = data.getData();
                requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
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

        if (selectedImages.isEmpty()) {
            layoutImagePlaceholder.setVisibility(View.VISIBLE);
            viewPagerSellImages.setVisibility(View.GONE);
            tabDotsSell.setVisibility(View.GONE);
            return;
        }

        layoutImagePlaceholder.setVisibility(View.GONE);
        viewPagerSellImages.setVisibility(View.VISIBLE);
        tabDotsSell.setVisibility(View.VISIBLE);

        sellImageAdapter =
                new SellImageSliderAdapter(
                        requireContext(),
                        selectedImages,
                        () -> {
                            imagesModified = true;
                            selectedImages.clear();
                            selectedImages.addAll(sellImageAdapter.getImages());
                            showSelectedImages();
                            updateImageUploadButtonUI();
                        }
                );

        viewPagerSellImages.setAdapter(sellImageAdapter);
        new TabLayoutMediator(tabDotsSell, viewPagerSellImages, (t, p) -> {}).attach();
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
            selectedImages.addAll(productToEdit.getImageUrls());
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
            b.setChecked(b.getText().toString().equals(category));
            if (b.isChecked()) selectedCategory = category;
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
            b.setChecked(b.getText().toString().equals(condition));
            if (b.isChecked()) selectedCondition = condition;
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

    // 🔥 SUBMIT FORM TO FIREBASE -------------------------------------------
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
        try { price = Double.parseDouble(priceStr); }
        catch (Exception e) { toast("Invalid price"); return; }

        if (price > 0 && qrPaymentUri == null) {
            toast("QR required for paid items");
            return;
        }

        int usedDays = usedStr.isEmpty() ? 0 : Integer.parseInt(usedStr);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            toast("Please login.");
            return;
        }

        String sellerId = firebaseUser.getUid();


        Product p = new Product(
                id, name, price, new ArrayList<>(), desc,
                selectedCondition, usedDays,
                "Available", selectedCategory, location,
                sellerId, qrPaymentUri != null ? qrPaymentUri.toString() : null
        );

        p.setImageVersion(System.currentTimeMillis());

        // Upload images + save product
        uploadImagesAndSaveProduct(p);
    }

    private void uploadImagesAndSaveProduct(Product p) {
        if (selectedImages.isEmpty()) {
            saveProductToFirestore(p);
            return;
        }

        List<String> downloadUrls = new ArrayList<>();
        String productId = p.getId();

        for (int i = 0; i < selectedImages.size(); i++) {
            Uri uri = Uri.parse(selectedImages.get(i));
            StorageReference ref = storage.getReference()
                    .child("product_images/" + productId + "/img_" + i);

            int index = i;
            ref.putFile(uri)
                    .continueWithTask(task -> ref.getDownloadUrl())
                    .addOnSuccessListener(url -> {
                        downloadUrls.add(url.toString());
                        if (downloadUrls.size() == selectedImages.size()) {
                            p.setImageUrls(downloadUrls);
                            saveProductToFirestore(p);
                        }
                    })
                    .addOnFailureListener(e -> toast("Image upload failed"));
        }
    }

    private void saveProductToFirestore(Product p) {
        db.collection("products")
                .document(p.getId())
                .set(p)
                .addOnSuccessListener(v -> {
                    toast("Item posted!");
                    NavHostFragment.findNavController(this).popBackStack();
                })
                .addOnFailureListener(e -> toast("Failed to post item"));
    }

    // UPDATE ITEM (LOCAL / FIREBASE) --------------------------------------
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
        try { price = Double.parseDouble(priceStr); }
        catch (Exception e) { toast("Invalid price"); return; }

        if (price > 0 && qrPaymentUri == null && productToEdit.getQrPaymentUrl() == null) {
            toast("QR required for paid items");
            return;
        }

        int usedDays = usedStr.isEmpty() ? 0 : Integer.parseInt(usedStr);

        productToEdit.setName(name);
        productToEdit.setDescription(desc);
        productToEdit.setPrice(price);
        productToEdit.setLocation(location);
        productToEdit.setCategory(selectedCategory);
        productToEdit.setCondition(selectedCondition);
        productToEdit.setUsedDaysTotal(usedDays);

        if (imagesModified) {
            List<String> newImages = new ArrayList<>();
            for (Object obj : selectedImages) newImages.add(obj.toString());
            productToEdit.setImageUrls(newImages);
            productToEdit.setImageVersion(System.currentTimeMillis());
        }

        if (qrPaymentUri != null) productToEdit.setQrPaymentUrl(qrPaymentUri.toString());

        // Save to Firestore
        db.collection("products")
                .document(productToEdit.getId())
                .set(productToEdit)
                .addOnSuccessListener(v -> {
                    toast("Item updated!");
                    navigateBackAfterSave();
                })
                .addOnFailureListener(e -> toast("Failed to update item"));
    }

    private void navigateBackAfterSave() {
        if (origin != null) requireActivity().finish();
        else NavHostFragment.findNavController(this).popBackStack();
    }

    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
