package app_library.comunication;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

import app_library.MainApplication;

/**
 * Created by User on 20/06/2018.
 */

/**
 * Classe che implementa le richieste HTTP (DELETE, GET, POST e PUT impacchettando eventuali messaggi come parametro) necessarie alla classe ServerComunication
 * L'url della risorsa sarà http://[ipserver]:3000/[uri della risorsa]
 */

public class HttpRequest extends AsyncTask<String,Void,String> {

    // posrta del server
    private static final String PORT = "3000";

    // costanti per il tipo di richiesta
    public static final String DELETE_REQUEST = "DELETE";
    public static final String GET_REQUEST = "GET";
    public static final String POST_REQUEST = "POST";
    public static final String PUT_REQUEST = "PUT";

    // metodo in background per l'esecuzione della richiesta al server
    // in urls[0] vi è il tipo di richiesta, urls[1] vi è l'indirizzo ip del server, urls[2] vi è l'uri della risorsa, urls[3] ci sono eventuali argomenti della richiesta
    @Override
    protected String doInBackground(String... urls)
    {
        String result = "";
        URL url = null;
        HttpURLConnection connection = null;

        // costituzione url
        try {
            url = new URL("http://" + urls[1] + ":" + PORT + "/" + urls[2]);
            Log.i("URL","url: " + url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // apertura connessione
        try {
            connection = (HttpURLConnection) url.openConnection();
        }catch (SocketTimeoutException e1){
            Toast.makeText(MainApplication.getCurrentActivity().getApplicationContext(), "Connessione al server scaduta, riavviare l'applicazione", Toast.LENGTH_SHORT).show();
            MainApplication.setOnlineMode(false);
            Log.e("errore","sessione scadura");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // richiesta delete
        if (urls[0].equals(HttpRequest.DELETE_REQUEST))
        {
            connection.setConnectTimeout(5000);

            try {

                connection.setRequestMethod("DELETE");

            }catch (IOException e) {
                e.printStackTrace();
            }

            // esecuzione delete
            try {
                if (connection.getResponseCode() == 200) {
                    InputStreamReader is = new InputStreamReader(connection.getInputStream());
                    BufferedReader read = new BufferedReader(is);
                    String s = null;
                    StringBuffer sb = new StringBuffer();
                    try {
                        while ((s = read.readLine()) != null) {
                            sb.append(s);
                        }
                        read.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        is.close();
                    }
                    result = sb.toString();

                }
                else if (connection.getResponseCode()==201) {
                    result = "delete effettuata con successo";
                }
                else if (connection.getResponseCode()==500) {
                    result = "delete non andata a buon fine";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (connection!=null) connection.disconnect();
            }
        }
        // richiesta get
        else if (urls[0].equals(HttpRequest.GET_REQUEST))
        {
            connection.setConnectTimeout(2000);

            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }

            // esecuzione get
            try {
                //se la comunicazione è andata a buon fine, il server risponderà con il codice 200, quindi potremmo
                //prendere il messaggio di risposta e restituirlo come output
                if (connection.getResponseCode() == 200) {
                    InputStreamReader is = new InputStreamReader(connection.getInputStream());
                    BufferedReader read = new BufferedReader(is);
                    String s = null;
                    StringBuffer sb = new StringBuffer();
                    try {
                        while ((s = read.readLine()) != null) {
                            sb.append(s);
                        }
                        read.close();
                        result = sb.toString();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        is.close();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection!=null) connection.disconnect();
            }
        }
        // richiesta post
        else if (urls[0].equals(HttpRequest.POST_REQUEST))
        {
            connection.setConnectTimeout(5000);

            // esecuzione post
            try {

                connection.setDoOutput(true);   //abilita la scrittura
                connection.setRequestMethod("POST");
                //scritto header http del messaggio (per inviare json)
                connection.setRequestProperty("Content-Type", "application/json");


                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                Log.i("json",urls[3]);
                wr.write(urls[3]);
                wr.flush();
                wr.close();

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (connection.getResponseCode() == 200) {
                    InputStreamReader is = new InputStreamReader(connection.getInputStream());
                    BufferedReader read = new BufferedReader(is);
                    String s = null;
                    StringBuffer sb = new StringBuffer();
                    try {
                        while ((s = read.readLine()) != null) {
                            sb.append(s);
                        }
                        read.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        is.close();
                    }
                    result = sb.toString();

                }
                else if (connection.getResponseCode()==201) {
                    result = "true";
                }
                else if (connection.getResponseCode()==500) {
                    result = "false";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    Log.i("Response"," " + connection.getResponseCode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (connection!=null) connection.disconnect();

            }
        }
        // richiesta put
        else
        {
            connection.setConnectTimeout(5000);

            // esecuzione put
            try {

                connection.setDoOutput(true);   //abilita la scrittura
                connection.setRequestMethod("PUT");
                //scritto header http del messaggio (per inviare json)
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                //vado a creare un writer che permette di iniettare il messaggio json (urls[2])all'interno del corpo
                //del messaggio
                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(urls[3]);
                wr.flush();
                wr.close();


            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (connection.getResponseCode() == 200) {
                    InputStreamReader is = new InputStreamReader(connection.getInputStream());
                    BufferedReader read = new BufferedReader(is);
                    String s = null;
                    StringBuffer sb = new StringBuffer();
                    try {
                        while ((s = read.readLine()) != null) {
                            sb.append(s);
                        }
                        read.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        is.close();
                    }
                    result = sb.toString();

                }
                else if (connection.getResponseCode()==201) {
                    result = "post effettuata con successo";
                }
                else if (connection.getResponseCode()==500) {
                    result = "post non andata a buon fine";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (connection!=null) connection.disconnect();
            }
        }

        return result;
    }
}
