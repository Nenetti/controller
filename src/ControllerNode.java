import javax.swing.JFrame;

import org.ros.RosRun;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import ros.NodeHandle;
import ros.Publisher;
import ros.Subscriber;
import sensor_msgs.Joy;

public class ControllerNode extends NodeHandle {

	public static double linearVelocity=1;
	public static double angularVelocity=1;
	
	private Publisher publisher;

	private double linear;
	private double angular;

	private boolean isJoy;
	
	/******************************************************************************
	 * 
	 */
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("rosjava/controller");
	}
	
	/******************************************************************************
	 * 
	 */
	public ControllerNode() {
		System.out.println(NodeHandle.connectedNode());
		JFrame frame=new JFrame("コントローラー");
		JFXPanel panel=new JFXPanel();
		frame.add(panel);
		AnchorPane root=new AnchorPane();
		root.setStyle("-fx-background-color: #000000;");
		Scene scene=new Scene(root);
		
		Circle circle=new Circle(2);
		Label label=new Label();
		
		
		root.getChildren().add(circle);
		panel.setScene(scene);
		frame.setBounds(0, 0, 110, 160);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
	}


	
	/******************************************************************************
	 * 
	 */
	@Override
	public void start() {
		Subscriber subscriber=new Subscriber("joy", Joy._TYPE);
		publisher=new Publisher("", type)
		subscriber.addMessageListener((message)->{
			isJoy=true;
			float[] axes=((Joy)message).getAxes();
			//int[] buttons=message.getButtons();
			angular=((axes[3]-1)*-1)/2+(axes[4]-1)/2;
			linear=(axes[1]+axes[5])/2;
		});
		
		
		
		/*
		publisher=new VelocityPublisher(connectedNode);
		
		
		
		Subscriber<sensor_msgs.Joy> subscriber = connectedNode.newSubscriber("joy", sensor_msgs.Joy._TYPE);
		subscriber.addMessageListener(new MessageListener<sensor_msgs.Joy>() {
			@Override
			public void onNewMessage(sensor_msgs.Joy message) {
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
		}).start();*/
	}


	private void duration(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
		}
	}
}
