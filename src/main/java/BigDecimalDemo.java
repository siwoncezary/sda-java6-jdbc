import java.math.BigDecimal;

public class BigDecimalDemo {
    public static void main(String[] args) {
        float a = 0.1f;
        float b = 0.1f;
        System.out.println("Obliczenia na float");
        System.out.println(a*b*10_000_000);
        BigDecimal c = new BigDecimal("0.1");
        BigDecimal d = new BigDecimal("0.1");
        System.out.println(c.multiply(d).multiply(new BigDecimal(10_000_000)));
    }
}
