package com.example.it_o_coiso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import android.content.pm.ActivityInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Atividade responsável pelos comandos enviados para o carrinho
 * */
public class ControlActivity extends AppCompatActivity {
    RelativeLayout layout_joystick;

    int lightStatus = 0;

    JoyStickClass js;

    ConnectionThread connect;

    String connection_bluetooth;

    BluetoothAdapter btAdapter ;

    int lastDirection= 0;

    /*Create da tela*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_activity);

        Connect();

        ImageButton light_image = (ImageButton) findViewById(R.id.btn_light);

        layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);

        /*Carrega a tela anterior*/
        Intent intent = this.getIntent();

        /*Pega a string de conexão passada na outra tela*/
        connection_bluetooth = intent.getStringExtra("connection_bluetooth");

        /**
         * A variável responsável por controlar o joystick é instanciada passando-se o seu layout respectivo e a
         * imagem que deverá carregar quando o o joystick for movimentado
         */
        js = new JoyStickClass(getApplicationContext(), layout_joystick, R.drawable.joystick_pt2);

        /* Os tamanhos que tanto a imagem do fundo do joystick e o próprio deverão possuir*/
        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(250);
        js.setOffset(90);
        js.setMinimumDistance(50);

        /*essa função você pode usar pra verificar a direção que o joystick está apontando*/
        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {

                    int direction = js.get4Direction();

                    switch (direction){
                        case JoyStickClass.STICK_RIGHT:
                            if(direction != lastDirection){
                                lastDirection = JoyStickClass.STICK_RIGHT;
                                Write("direcao=1;");
                            }
                            break;

                        case JoyStickClass.STICK_LEFT:
                            if(direction != lastDirection) {
                                lastDirection = JoyStickClass.STICK_LEFT;
                                Write("direcao=-1;");
                            }
                            break;

                        default:
                            if(lastDirection!= 0 ) {
                                lastDirection = 0;
                                Write("direcao=0;");
                            }
                            break;
                    }
                }

                return true;
            }
        });

        /*Botão responsável por reconectar ao bluetooth*/
        ImageButton btnReconnect = (ImageButton) findViewById(R.id.btn_reconnect);
        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Connect();
            }
        });

        /*Botão responsável por freiar o carrinho*/
        ImageButton btnStop = (ImageButton) findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Write("btnFreio;");
            }
        });

        /*Botão responsável por acelerar o carrinho*/
        ImageButton btnAcelerate = (ImageButton) findViewById(R.id.btn_acelerate);
        btnAcelerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Write("btnFrente=1;");
            }
        });

        /*Botão responsável por dar ré no carrinho*/
        ImageButton btnReverse = (ImageButton) findViewById(R.id.btn_reverse);
        btnReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Write("btnTras=1;");
            }
        });

        /*Botão responsável pela buzina do carrinho*/
        ImageButton btnHorn = (ImageButton) findViewById(R.id.btn_horn);
        btnHorn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Write("btnBuzina=1;");
            }
        });

        /*Botão responsável por ligar e desligar o farol do carrinho. Por padrão, o carrinho começa com esse farol desligado*/
        ImageButton btnLight = (ImageButton) findViewById(R.id.btn_light);
        btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                light_image.setImageResource(lightStatus == 1 ? R.drawable.light_off : R.drawable.light_on);
                Write(("btnFarol="+ String.valueOf(lightStatus) + ";"));
                lightStatus = lightStatus == 1 ? 0 : 1;
            }
        });
    }

    /**
     * Método que envia uma instrução para o módulo bluetooth do carrinho
     * */
    public void Write(String param){

        connect.write(param.getBytes());
    }

    /**
     * Método responsável por conectar ao bluetooth
     * */
    public void Connect(){
        /*Teste Bluetooth e conexão*/
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Intent intent_erro = new Intent(ControlActivity.this, InitialActivity.class);

            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
            ActivityCompat.startActivity(ControlActivity.this, intent_erro, activityOptionsCompat.toBundle());

            Toast.makeText(getApplicationContext(), "Deu ruim maluco", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Deu bom maluco", Toast.LENGTH_SHORT).show();
        }

        btAdapter.enable();

        //Mac do módulo bluetooth
        if(connection_bluetooth == null || connection_bluetooth.isEmpty())
            connect = new ConnectionThread( "00:19:07:00:21:D7" );
        else
            connect = new ConnectionThread( connection_bluetooth );

        connect.start();

        try {
            Thread.sleep(200);
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(@org.jetbrains.annotations.NotNull Message msg) {

            /* Esse método é invocado na Activity principal sempre que a thread de conexão Bluetooth recebe uma mensagem*/
            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            String dataString= new String(data);
        }
    };
}