import javax.swing.JFrame;

import org.ros.RosRun;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;

import geometry_msgs.Twist;
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
import nav_msgs.Odometry;
import ros.NodeHandle;
import ros.Publisher;
import ros.Subscriber;
import sensor_msgs.Joy;

public class ControllerNode extends NodeHandle {

	public static double linearVelocity=1;
	public static double angularVelocity=1;

	private Publisher publisher;
	private Odometry odometry;

	private double linear;
	private double angular;
	private double odom_linear;
	private double odom_angular;

	private boolean isJoy;
	private boolean isOdom;
	private boolean isProcess;
	
	Label angularLabel;
	Label linearLabel;
	Label joy_status;
	Label odom_status;

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
		JFrame frame=new JFrame("コントローラー");
		JFXPanel panel=new JFXPanel();
		frame.add(panel);
		AnchorPane root=new AnchorPane();
		root.setStyle("-fx-background-color: #000000;");
		Scene scene=new Scene(root);

		angularLabel=new Label();
		linearLabel=new Label();
		joy_status=new Label();
		odom_status=new Label();
		setLabel(angularLabel, "0", Color.WHITE);
		setLabel(linearLabel, "0", Color.WHITE);
		setLabel(joy_status, "Not Connected Controller", Color.RED);
		setLabel(odom_status, "Not received Odometry", Color.RED);

		root.getChildren().addAll(angularLabel, linearLabel, joy_status, odom_status);
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
	 * @param label
	 * @param text
	 * @param color
	 */
	public void setLabel(Label label, String text, Color color) {
		Platform.runLater(()->{
			label.setText(text);
			label.setTextFill(color);
		});
	}

	/******************************************************************************
	 * 
	 */
	@Override
	public void start() {
		publisher=new Publisher("/cmd_vel_mux/input/teleop", Twist._TYPE);
		Subscriber joy_subscriber=new Subscriber("joy", Joy._TYPE);
		joy_subscriber.addMessageListener((message)->{
			isJoy=true;
			setLabel(joy_status, "Not Connected Controller", Color.RED);
			float[] axes=((Joy)message).getAxes();
			angular=((axes[3]-1)*-1)/2+(axes[4]-1)/2;
			linear=(axes[1]+axes[5])/2;
		});
		Subscriber odom_subscriber=new Subscriber("odom", Odometry._TYPE);
		odom_subscriber.addMessageListener((message)->{
			isOdom=true;
			setLabel(odom_status, "Not received Odometry", Color.RED);
			Twist twist=((Odometry)message).getTwist().getTwist();
			odom_angular=twist.getAngular().getZ();
			odom_linear=twist.getLinear().getX();
		});
		loop();
	}

	/******************************************************************************
	 * 
	 */
	public void loop() {
		new Thread(()-> {
			while(true) {
				if(!isProcess) {
					isProcess=true;
					Platform.runLater(()->{
						isProcess=false;
					});
				}
				if(linear!=0||angular!=0) {
					Twist twist=(Twist)publisher.newMessage();
					twist.getAngular().setZ(angular);
					twist.getLinear().setX(linear);
					publisher.publish(twist);
				}
				duration(1);
			}
		}).start();
	}
}
