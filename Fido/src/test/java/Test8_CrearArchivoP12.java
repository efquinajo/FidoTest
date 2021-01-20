
import gob.adsib.fido.data.KeyAndCertificate;
import gob.adsib.fido.stores.PkcsN12Manager;
import java.io.File;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author ADSIB-UID
 */
public class Test8_CrearArchivoP12 {
    public static void main(String cor[]) throws Exception{
        Security.addProvider(new BouncyCastleProvider());
        File fileP12 = new File("C:\\Users\\GIGABYTE\\Downloads\\CERTIFICADO\\CERTIFICADO\\keysotre_gonzalo_ramiro_nuevo.p12");
        String pin = "GonzaloPwC2020*";
        String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
"MIIEpAIBAAKCAQEA4NuR4Og6Lgfkt5pfBwda3j79HDyLuQ1MNk+x9Y5j1ch6DFiq\n" +
"/Fywyw9HZAfg5oB2YFE5SK7skI+KIiKswtIKMEaPbXfaPEDt8TZuHgh0w9Txob7J\n" +
"waEcI5kgcd1a0ObRDl76OvbbS3VL7mvjHi2nXdvpShdClg5IHZEeI7BUZGfeC8S6\n" +
"AImlMQ3zZwdBCtEOWlIIYzWzHSVFG40X/Vs9Sn218K2lHKtmR+gyrs7u53BqlWlN\n" +
"oGeJ2JcSs9mZo8hZtEU2bdvHdSBctDysREEJskKJ0oEwhx8dDqUi6s3sjXuSE7CU\n" +
"ZJVjRnXTFvtiP+Zy3x1dIear97IEE8I9EpS1OQIDAQABAoIBAEZXqh0nyPuZcI2X\n" +
"d17liCG6psfskPFFHbBn4g4li0nXu4adPnBQNuZvUzAR3uN9EIs1HayFPHBE1zuy\n" +
"iUUxHAraKAhg1B7BWS56jpxKnOyeczDcVJWEZx8jyiiUzNJJQuEyCSnlC9lhSm29\n" +
"xoEsWw1bvHF3MVdsBFdrjHgxs1TtK0YUZ7Fj7VWP4dAlcikIVe5UYR0TR8vOZ4YF\n" +
"3pzSROZ/Z8GKSfKnFDvrsX7EIWww4DBVPWpjEkPlLAE0sH1/X9MtoMdJbjtP4T8x\n" +
"muezRMUcxkjlvcDG4F+YfzDfihhyYWRHD61dVsZX4GzmlZX1ynaKvnnGfIWUfamf\n" +
"eByBcAECgYEA8QwrPjlg5S1ChIIW2OWMk06Reior+T64cUCPDQeAYdYEBGfIH9SZ\n" +
"iJtjYXO9sQghUvq8MR77bzBn5Jf/WFFdSeq1RokKKrTw3a+ewU2QnEk4zsJd70c4\n" +
"1a/V1E7tJtG0Vp7oVTtBDW+hwFX3r8Xm8a/74zQYSrMMLqSgzYouAwECgYEA7s5O\n" +
"d4j7EKjaJnJfFYdBi/grWQlow1hXdXn6qdslx73XvUKlfu00XiX9PshFNWCdxyaI\n" +
"epWLaY9kmVbBb4UdjauTiKSvYU4oGN1GAtJZ3U9yOdsejULwOo2fpf92P+1rQhDb\n" +
"4E7Z9wuDgA1FGIj9YGpG8sSDdOvUnjEodNo4CjkCgYEAt3qYy+NnEusRj8Vp2K1a\n" +
"BKKwb3n8QNtyD5qhiLxmB5KdOjE5DqmIOIh27C3qfP+ARiZe61D2+FqzKjhcgABm\n" +
"7yW92DPlzj4ufb/5KpB8+8lseU1PrRvcciNGszVkpMDI8YpBtObGjJClYb7OKziL\n" +
"ovpe1EHKH8oMAtEDKZD9lwECgYEAjqjiiVhkm+QzRkqG8QZ3KtXbl3oegqxQuxZe\n" +
"n4AShsuriR26XiP4Z9IMAqiDZ8rLFsC3QXv659nIwC3qquN77zkzjqrLNrcJDymI\n" +
"/ICLPMGMPHHhQ4RcnK4kVHdTPgdoTvRhVhPk9EeFjEbhIzIbI7D5p2esHHoR71rw\n" +
"jzYZykkCgYAhpjKyUDjQT8nyzTtATLIPBQ/H6oG2drkJb+KuSgnppP3LU/aWmUbC\n" +
"JZJ57p6Hhzmscp2Sv8P6Vd/vQR0PfRWeu2LQHvImErRWeqesJ9rxw2txASr1XsXI\n" +
"VjbWUI6oIsbrsOd/kAiwDex0zMh0Ak4tFXMUjb6+V/3Q+rsb2Zr1Yg==\n" +
"-----END RSA PRIVATE KEY-----";
        String pemCertificado="-----BEGIN CERTIFICATE-----\n" +
"MIIHdTCCBV2gAwIBAgIIcartMfX2agMwDQYJKoZIhvcNAQELBQAwSzEsMCoGA1UE\n" +
"AwwjRW50aWRhZCBDZXJ0aWZpY2Fkb3JhIFB1YmxpY2EgQURTSUIxDjAMBgNVBAoM\n" +
"BUFEU0lCMQswCQYDVQQGEwJCTzAeFw0yMDA0MjQxNTU3MDBaFw0yMTA0MjMxMzAz\n" +
"MDBaMIIBCDELMAkGA1UELhMCQ0kxKDAmBgNVBAMMH0dPTlpBTE8gUkFNSVJPIEFS\n" +
"SVNDQUlOIEJFUk5JTkkxEzARBgNVBAUTCjEwMjAzNzkwMjQxHDAaBgNVBAwME1Jl\n" +
"cHJlc2VudGFudGUgTGVnYWwxGDAWBgNVBAsMD0FkbWluaXN0cmFjacOzbjEmMCQG\n" +
"A1UECgwdUFJJQ0VXQVRFUkhPVVNFQ09PUEVSUyBTLlIuTC4xCzAJBgNVBAYTAkJP\n" +
"MRQwEgYHKwYBAQEBAAwHMjM5NjgyMDE3MDUGA1UEDQwuUGVyc29uYSBKdXJpZGlj\n" +
"YSBTZWd1cmlkYWQgTm9ybWFsIEZpcm1hIFNpbXBsZTCCASIwDQYJKoZIhvcNAQEB\n" +
"BQADggEPADCCAQoCggEBAODbkeDoOi4H5LeaXwcHWt4+/Rw8i7kNTDZPsfWOY9XI\n" +
"egxYqvxcsMsPR2QH4OaAdmBROUiu7JCPiiIirMLSCjBGj2132jxA7fE2bh4IdMPU\n" +
"8aG+ycGhHCOZIHHdWtDm0Q5e+jr220t1S+5r4x4tp13b6UoXQpYOSB2RHiOwVGRn\n" +
"3gvEugCJpTEN82cHQQrRDlpSCGM1sx0lRRuNF/1bPUp9tfCtpRyrZkfoMq7O7udw\n" +
"apVpTaBnidiXErPZmaPIWbRFNm3bx3UgXLQ8rERBCbJCidKBMIcfHQ6lIurN7I17\n" +
"khOwlGSVY0Z10xb7Yj/mct8dXSHmq/eyBBPCPRKUtTkCAwEAAaOCApwwggKYMHkG\n" +
"CCsGAQUFBwEBBG0wazA7BggrBgEFBQcwAoYvaHR0cHM6Ly93d3cuZmlybWFkaWdp\n" +
"dGFsLmJvL2Zpcm1hZGlnaXRhbF9iby5wZW0wLAYIKwYBBQUHMAGGIGh0dHA6Ly93\n" +
"d3cuZmlybWFkaWdpdGFsLmJvL29jc3AvMB0GA1UdDgQWBBQLcPDoLqtqu4ArFnWE\n" +
"jv3MuxeNHzAJBgNVHRMEAjAAMB8GA1UdIwQYMBaAFNKZ3cFvJS4nqAvr3NnWklti\n" +
"VaDCMIHeBgNVHSAEgdYwgdMwUAYOYEQAAAABDgECAAEAAAAwPjA8BggrBgEFBQcC\n" +
"ARYwaHR0cHM6Ly93d3cuZmlybWFkaWdpdGFsLmJvL3BvbGl0aWNhanVyaWRpY2Eu\n" +
"cGRmMH8GD2BEAAAAAQ4BAgABAgAAADBsMGoGCCsGAQUFBwICMF4eXABQAGUAcgBz\n" +
"AG8AbgBhACAASgB1AHIAaQBkAGkAYwBhACAAUwBlAGcAdQByAGkAZABhAGQAIABO\n" +
"AG8AcgBtAGEAbAAgAEYAaQByAG0AYQAgAFMAaQBtAHAAbABlMIGTBgNVHR8EgYsw\n" +
"gYgwgYWgMqAwhi5odHRwOi8vd3d3LmZpcm1hZGlnaXRhbC5iby9maXJtYWRpZ2l0\n" +
"YWxfYm8uY3Jsok+kTTBLMSwwKgYDVQQDDCNFbnRpZGFkIENlcnRpZmljYWRvcmEg\n" +
"UHVibGljYSBBRFNJQjEOMAwGA1UECgwFQURTSUIxCzAJBgNVBAYTAkJPMAsGA1Ud\n" +
"DwQEAwIE8DAnBgNVHSUEIDAeBggrBgEFBQcDAgYIKwYBBQUHAwMGCCsGAQUFBwME\n" +
"MCMGA1UdEQQcMBqBGGdvbnphbG8uYXJpc2NhaW5AcHdjLmNvbTANBgkqhkiG9w0B\n" +
"AQsFAAOCAgEAHm+PmIjVqShfUOx7I4uT5dxvKXOCuz3673akvuF5TFZIcdMWhXpo\n" +
"bQD8FL0Rqu88lFv1mrRkELIqQ275fVr6Ju+/u7vpbpc3qzUBxG/DGcUkUfma7bjQ\n" +
"ILmT58Wj8dgnRI0s6BEe1Rp2jMAT8IqjswZeBdBtlaBpox024LuUF7cIh7YTw8FV\n" +
"m5OspEhNuWNZRLbwqWwz1eOzRv1MUarURf5tzabT37OKAtHdDuKrKcm/c8SjCgCm\n" +
"qqN360tiI7t4ZL5IY4cBJZ/V/Sx4NgC/6ktdMl3biFr0ryQxUSk3RHXjJIzGGz3d\n" +
"bNvklDR3m00vAz1KrSqnPTTr0OQzaPOltMZSuyDZxNKOmwjzFKQ/9iB8oU60Fpw+\n" +
"Z4Y2DMGTwwHjktp8Log5MSrho+/Sv0dKaPvHRIBTpshlOu0MisziTiI29W1CV45D\n" +
"OQOtE+yedMYBAoiMv2IuDPp+SbW0Oj4EaDmFn4mTwNYsmv6gMwORMq+lIjNEY+Hf\n" +
"qMIvlJUKL5hTBsWazGla0FNj2hcqEsNf9ixtUyFDY7MuFJuiMtMgGg8KABfRMlMT\n" +
"1/g+S0uPcfXizHdXHef3w5AJKM/IfPCtxhc2/Fosp3xUo8g4JzwXk8wqGzxn8Ltz\n" +
"19Zn97nh1P5HeOLjqyX7uG7C2D1MAoaVE6Wl/T0UVKsVGzT4G1I61pk=\n" +
"-----END CERTIFICATE-----";
        String alias="gonzalo_ramiro";
        
        PkcsN12Manager pkcsN12Manager = new PkcsN12Manager(fileP12);
        pkcsN12Manager.createNewKeyStoreFile(pin);
//        pkcsN12Manager.login(pin, 0);
        KeyAndCertificate keyAndCertificate = new KeyAndCertificate(privateKey, pemCertificado, alias);
        pkcsN12Manager.addOrUpdateKeyAndCertificate(keyAndCertificate);
        pkcsN12Manager.save();
        
        
        pkcsN12Manager.logout();
    }
}