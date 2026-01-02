package naeil.gen_coupon.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import naeil.gen_coupon.common.util.CryptoUtil;

import javax.crypto.SecretKey;

@Converter
public class EncryptConverter implements AttributeConverter<String, String> {

    private static SecretKey secretKey;

    public EncryptConverter(SecretKey secretKey) {
        EncryptConverter.secretKey = secretKey;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return CryptoUtil.encrypt(attribute, secretKey);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return CryptoUtil.decrypt(dbData, secretKey);
    }
}
