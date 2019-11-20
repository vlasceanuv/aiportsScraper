package Firebase;

import Dto.AirlinesDTO;
import Dto.AirportDto;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FirebaseConfig {
    // Use a service account

    private static Firestore db;

    public void connect() throws IOException {
//         Main.class.getResourceAsStream("/firebase.json");
        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .build();
        FirebaseApp.initializeApp(options);

        db = FirestoreClient.getFirestore();
    }

    public void add(Map<String, AirportDto> list) {
        WriteBatch batch = db.batch();
        list.values().forEach(dto -> {
            String documetName = "";
            if (dto.IATA != null) {
                documetName = dto.IATA;
            } else if (dto.ICAO != null) {
                documetName = "ICAO-" + dto.ICAO;
            } else if (dto.FAA != null) {
                documetName = "FAA-" + dto.IATA;
            } else {
                documetName = UUID.randomUUID().toString();
            }

            DocumentReference docRef = db.collection("airports").document(documetName);
            batch.set(docRef, dto, SetOptions.merge());

        });
        ApiFuture<List<WriteResult>> future = batch.commit();
//        try {
//            for (WriteResult result :future.get()) {
//                System.out.println("Update time : " + result.getUpdateTime());
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
    }

    public void addAirlines(Map<String,AirlinesDTO> airlines) {
        WriteBatch batch = db.batch();
        airlines.values().forEach(dto -> {

            DocumentReference docRef = db.collection("airline").document(dto.code);
            batch.set(docRef, dto, SetOptions.merge());
        });
        ApiFuture<List<WriteResult>> future = batch.commit();
        try {
            for (WriteResult result :future.get()) {
                //System.out.println("Update time : " + result.getUpdateTime());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
