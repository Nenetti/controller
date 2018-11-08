import javax.swing.JFrame;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ControllerNode extends AbstractNodeMain{

	private double linearVelocity=1;
	private double angularVelocity=1;
	private VelocityPublisher publisher;

	private double linear;
	private double angular;

	private boolean isJoy;

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("rosjava/controller");
	}
	@Override
	public void onStart(ConnectedNode connectedNode) {
		
		
		JFrame frame=new JFrame("コントローラー");
		JFXPanel panel=new JFXPanel();
		frame.add(panel);
		AnchorPane root=new AnchorPane();
		root.setStyle("-fx-background-color: #000000;");
		Scene scene=new Scene(root);
		final Label label_W=new Label("W");
		label_W.setAlignment(Pos.CENTER);
		label_W.relocate(50, 25);
		label_W.setFont(new Font(20));
		label_W.setTextFill(Color.GRAY);
		final Label label_A=new Label("A");
		label_A.setAlignment(Pos.CENTER);
		label_A.relocate(25, 50);
		label_A.setFont(new Font(20));
		label_A.setTextFill(Color.GRAY);
		final Label label_S=new Label("S");
		label_S.setAlignment(Pos.CENTER);
		label_S.relocate(50, 75);
		label_S.setFont(new Font(20));
		label_S.setTextFill(Color.GRAY);
		final Label label_D=new Label("D");
		label_D.setAlignment(Pos.CENTER);
		label_D.relocate(75, 50);
		label_D.setFont(new Font(20));
		label_D.setTextFill(Color.GRAY);
		final Label label_Linear=new Label();
		//label_Linear.setAlignment(Pos.CENTER);
		label_Linear.relocate(20, 110);
		label_Linear.setFont(new Font(15));
		label_Linear.setTextFill(Color.WHITE);
		final Label label_Angular=new Label();
		//label_Angular.setAlignment(Pos.CENTER);
		label_Angular.relocate(20, 140);
		label_Angular.setFont(new Font(15));
		label_Angular.setTextFill(Color.WHITE);
		
		root.getChildren().addAll(label_W, label_A, label_S, label_D, label_Linear, label_Angular);
		panel.setScene(scene);
		frame.setBounds(0, 0, 110, 160);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		publisher=new VelocityPublisher(connectedNode);

		Subscriber<sensor_msgs.Joy> subscriber = connectedNode.newSubscriber("joy", sensor_msgs.Joy._TYPE);
		subscriber.addMessageListener(new MessageListener<sensor_msgs.Joy>() {
			@Override
			public void onNewMessage(sensor_msgs.Joy message) {
				/*
		    	   axes[0]: L stick horizontal (left=+1, right=-1)
		    	   axes[1]: L stick vertical (up=+1, down=-1)
		    	   axes[2]: R stick horizontal (left=+1, right=-1)
		    	   axes[3]: L2 (neutral=+1, full accel=-1)
		    	   axes[4]: R2 (neutral=+1, full accel=-1)
		    	   axes[5]: R stick vertical (up=+1, down=-1)
		    	   axes[6]: Accelerometer Left(コントローラ左方向が正方向)
		    	   axes[7]: Accelerometer Front(コントローラ手前方向が正方向)
		    	   axes[8]: Accelerometer Up(コントローラ上方向が正方向)
		    	   axes[9]: Axis button(十字キー) LR（L=＋１, R=−１）
		    	   axes[10]:Axis button(十字キー) Up/Down（Up=＋１, Down=−１）
		    	   axes[11]: Jyrometer Roll (手前から見て右回り：＋、左回り：ー)
		    	   axes[12]: Jyrometer Yaw (上から見て左回り：ー、右回り：＋)
		    	   axes[13]: Jyrometer Pitch (ライトバー側を上げる：ー, 下げる：＋)
		    	   buttons[0]: □Square
		    	   buttons[1]: ×Cross
		    	   buttons[2]: ○Circle
		    	   buttons[3]: △Triangle
		    	   buttons[4]: L1
		    	   buttons[５]: R1
		    	   buttons[6]: L2 digital(1/3くらい引くと1になる）
		    	   buttons[7]: R2 digital(1/3くらい引くと1になる）
		    	   buttons[8]: Share
		    	   buttons[9]: Options
		    	   buttons[10]: L3
		    	   buttons[11]: R3
		    	   buttons[12]: PS button
		    	   buttons[13]: Touchpad button
				 */
				float[] axes=message.getAxes();
				//int[] buttons=message.getButtons();

				//PS4コントローラーと有線のLogicoolコントローラーではボタンの割り振り方が違うため

				angular=((axes[3]-1)*-1)/2+(axes[4]-1)/2;
				linear=(axes[1]+axes[5])/2;
				isJoy=true;
			}
		});
		scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
				case W:
					linear=linearVelocity;
					break;
				case A:
					angular=angularVelocity;
					break;
				case D:
					angular=-angularVelocity;
					break;
				case S:
					linear=-linearVelocity;
					break;
				case C:
					System.exit(0);
					break;
				default:
					break;
				}
			}
		});
		scene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
				case W:
					linear=0;
					break;
				case A:
					angular=0;
					break;
				case S:
					linear=0;
					break;
				case D:
					angular=0;
					break;
				default:
					break;
				}
			}
		});
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if(linear!=0) {
								if(linear>0) {
									label_W.setTextFill(Color.RED);
								}else {
									label_S.setTextFill(Color.RED);
								}
							}else {
								label_W.setTextFill(Color.GRAY);
								label_S.setTextFill(Color.GRAY);
							}
							if(angular!=0) {
								if(angular>0) {
									label_A.setTextFill(Color.RED);
								}else {
									label_D.setTextFill(Color.RED);
								}
							}else {
								label_A.setTextFill(Color.GRAY);
								label_D.setTextFill(Color.GRAY);
							}
							label_Linear.setText("X:  "+( publisher.getLinear() < 0 ? String.format("%.3f", publisher.getLinear()) : String.format(" %.3f", publisher.getLinear())));
							label_Angular.setText("Z:  "+( publisher.getAngular() < 0 ? String.format("%.3f", publisher.getAngular()) : String.format(" %.3f", publisher.getAngular())));
						}
					});
					publisher.publishVelocity(linear, angular);
					if(isJoy) {
						linear=0;
						angular=0;
					}
					duration(100);
				}
			}
		}).start();
	}


	private void duration(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
		}
	}
}
