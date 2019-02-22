package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Product;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.UPCAReader;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.IOException;
import java.io.File;
import java.util.concurrent.ConcurrentMap;

public class GetFoodByBarcode extends Command {
    GetFoodByBarcode(ConcurrentMap<String, Product> foodByUpcCache) {
        super(null, null, foodByUpcCache);
    }

    private boolean validateQueryByBarcodeArg(String arg) {
        return (arg.startsWith("--upc=<") || arg.startsWith("--img=<")) && arg.endsWith(">");
    }

    private String extractQueryByBarcodeArg(String arg) {
        int openingBracketIdx = arg.indexOf('<');
        int closingBracketIdx = arg.indexOf('>');

        return arg.substring(openingBracketIdx + 1, closingBracketIdx);
    }

    private String decodeBarcode(String fileName) {
        File file = new File(fileName);
        BufferedImage image = null;
        BinaryBitmap bitmap = null;
        Result result = null;

        try {
            image = ImageIO.read(file);
            int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
            bitmap = new BinaryBitmap(new HybridBinarizer(source));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap == null) {
            return null;
        }

        UPCAReader reader = new UPCAReader();

        try {
            result = reader.decode(bitmap);
            return result.getText();
        } catch (NotFoundException | FormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String executeHelper(String argument, boolean isPathToImg) {
        String barcode;
        barcode = (isPathToImg) ? decodeBarcode(argument) : argument;

        if (barcode != null) {
            if (foodByUpcCache.containsKey(barcode)) {
                return foodByUpcCache.get(barcode).toString();
            }
        } else {
            return "Invalid upc or image path.";
        }

        return "No product with this barcode=" + barcode + " has been found.";
    }

    @Override
    public String execute(String argument) {
        String[] splittedArgs = argument.split("\\|");

        if (validateQueryByBarcodeArg(splittedArgs[0])) {
            String arg = extractQueryByBarcodeArg(splittedArgs[0]);
            boolean isPathToImg = splittedArgs[0].startsWith("--img");

            return executeHelper(arg, isPathToImg);
        } else {
            return "Get food by barcode was called with an invalid argument(s).";
        }
    }
}
