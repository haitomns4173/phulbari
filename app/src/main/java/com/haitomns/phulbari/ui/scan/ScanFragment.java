package com.haitomns.phulbari.ui.scan;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;

import com.google.common.util.concurrent.ListenableFuture;
import com.haitomns.phulbari.FlowerDetailActivity;
import com.haitomns.phulbari.databinding.FragmentScanBinding;
import com.haitomns.phulbari.ml.FlowerClassification;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {

    private FragmentScanBinding binding;
    private ImageButton capture;
    private PreviewView previewView;
    private final int cameraFacing = CameraSelector.LENS_FACING_BACK;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startCamera(cameraFacing);
                }
            });

    private final ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                getActivity();
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Toast.makeText(getContext(), "Image selected: " + selectedImageUri, Toast.LENGTH_SHORT).show();
                        // Handle the selected image URI (e.g., display it in an ImageView or process it)
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScanViewModel scanViewModel = new ViewModelProvider(this).get(ScanViewModel.class);

        binding = FragmentScanBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        previewView = binding.cameraPreview;
        capture = binding.capture;
        ImageButton gallery = binding.gallery;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }

        gallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            selectImageLauncher.launch(intent);
        });

        return root;
    }

    public void startCamera(int cameraFacing) {
        int aspectRatio = aspectRatio(previewView.getWidth(), previewView.getHeight());
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(requireContext());

        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = listenableFuture.get();

                Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                ImageCapture imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(requireActivity().getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing)
                        .build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                capture.setOnClickListener(view -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                    takePicture(imageCapture);
                });

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    public void takePicture(ImageCapture imageCapture) {
        final File file = new File(requireContext().getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                //save the image to the gallery
                classifyImage(file.getAbsolutePath());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Failed to save: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
                startCamera(cameraFacing);
            }
        });
    }

    private int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    private void showToastOnMainThread(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void classifyImage(String imagePath) {
        // Load the image from the path
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap == null) {
            Toast.makeText(getContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Resize the bitmap to the model's input size (256x256)
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);

        // Convert the bitmap to a ByteBuffer
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(resizedBitmap);

        try {
            // Initialize the model
            FlowerClassification model = FlowerClassification.newInstance(getContext());

            // Create input tensor buffer
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Run model inference
            FlowerClassification.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Get the predicted class index
            float[] confidences = outputFeature0.getFloatArray();
            int maxIndex = getMaxConfidenceIndex(confidences);

            // List of flower classes
            String[] flowerClasses = {"Ageratum", "Amaryllis", "Anemone", "Angelonia", "Aster", "Bachelors Button",
                    "Balsam", "Begonia", "Bleeding Heart", "Bluebell Creeper", "Bottle Brush", "Bougainvillea",
                    "Buddleia", "Butterfly Pea", "Calendula", "Calla Lily", "Camellia", "Canna Lily", "Carnation",
                    "Celosia", "China Pink", "Chrysanthemum", "Clarkia", "Cocks Comb", "Coral Tree", "Coreopsis",
                    "Corydalis", "Cosmos", "Cranes Bill", "Crossandra", "Crown Flower", "Crown Imperial",
                    "Crown Of Thrones", "Cypress Vine", "Daffodil", "Dahlia", "Daisy", "Daylily",
                    "Firecracker Flower", "Forget-me-not", "Four O Clock", "Foxglove", "Frangipani",
                    "Gardenia", "Gazania", "Geranium", "Gerbera Daisy", "Gloxinia", "Gulmohar",
                    "Heliotrope", "Hibiscus", "Hollyhock", "Impatiens", "Iris", "Ixora", "Jacaranda",
                    "Jasmine", "Lantana", "Lavender", "Lily", "Lotus", "Lupine", "Marigold", "Marsh Marigold",
                    "Morning Glory", "Murraya", "Nasturtium", "Night Flowering Jasmine", "Oleander",
                    "Orchid", "Pansy", "Peony", "Peregrina", "Periwinkle", "Petunia", "Plumeria", "Poppy",
                    "Portulaca", "Primrose", "Purple Globe Amaranth", "Rangoon Creeper", "Rhododendrons",
                    "RockRose", "Rose", "Salvia", "Scarlet Sage", "Snapdragon", "Spider Lily", "Sunflower",
                    "Sweet Pea", "Tuberose", "Tulip", "Varigated Bauhinia", "Verbascum", "Verbena",
                    "Wax Mallow", "Winter Jasmine", "Yellow Bells", "Yellow Oleander", "Zinnia"};

            // Display the predicted class
            String predictedFlower = flowerClasses[maxIndex];
            showToastOnMainThread("Predicted flower: " + predictedFlower);
            showFlowerDetail(predictedFlower);
            

            // Close the model
            model.close();
        } catch (IOException e) {
            showToastOnMainThread("Model loading failed.");
        }
    }

    // In HomeFragment.java
    private void showFlowerDetail(String flowerName) {
        Intent intent = new Intent(getContext(), FlowerDetailActivity.class);
        intent.putExtra("flower_name", flowerName);
        startActivity(intent);
    }

    // Convert Bitmap to ByteBuffer
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 256 * 256 * 3); // Float size = 4 bytes
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[256 * 256];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixelValue : intValues) {
            // Normalize pixel values from 0-255 to 0-1
            float r = ((pixelValue >> 16) & 0xFF) / 255.f;
            float g = ((pixelValue >> 8) & 0xFF) / 255.f;
            float b = (pixelValue & 0xFF) / 255.f;
            byteBuffer.putFloat(r);
            byteBuffer.putFloat(g);
            byteBuffer.putFloat(b);
        }
        return byteBuffer;
    }

    // Get index of the highest confidence value
    private int getMaxConfidenceIndex(float[] confidences) {
        int maxIndex = 0;
        float maxConfidence = confidences[0];
        for (int i = 1; i < confidences.length; i++) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}