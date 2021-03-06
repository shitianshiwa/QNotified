package nil.nadph.qnotified.chiral;

import nil.nadph.qnotified.util.NonUiThread;
import nil.nadph.qnotified.util.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class PubChemStealer {

    @NonUiThread
    @Nullable
    public static Molecule nextRandomMolecule() {
        Random r = new Random();
        for (int retry = 5; retry > 0; retry--) {
            long cid = (long) (r.nextDouble() * 100000000 + r.nextDouble() * 10000000 + 100000);
            try {
                return getMoleculeByCid(cid);
            } catch (IOException e) {
                retry--;
            } catch (MdlMolParser.BadMolFormatException ignored) {
            }
        }
        return null;
    }

    @NonUiThread
    public static Molecule getMoleculeByCid(long cid) throws IOException, MdlMolParser.BadMolFormatException {
        HttpURLConnection conn = (HttpURLConnection) new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/CID/" + cid + "/record/SDF/?record_type=2d&response_type=display").openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            throw new IOException("Bad ResponseCode: " + conn.getResponseCode());
        }
        InputStream in = conn.getInputStream();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        in.close();
        conn.disconnect();
        String str = outStream.toString();
        return MdlMolParser.parseString(str);
    }
}
