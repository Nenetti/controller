
import javax.swing.JFrame;
import org.ros.namespace.GraphName;

import geometry_msgs.Twist;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
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

	private double linear;
	private double angular;
	private double odom_linear;
	private double odom_angular;

	private boolean isJoy;
	private boolean isOdom;
	private boolean isProcess;
	
	private Joy joy;
	
	Label angularLabel;
	Label linearLabel;
	Label joy_status;
	Label odom_status;
	
	Polygon arrowW;
	Polygon arrowA;
	Polygon arrowS;
	Polygon arrowD;
	
	private final static int width=200;
	private final static int height=240;
	private final static int arrowSize=20;

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
		Group root=new Group();
		Scene scene=new Scene(root);
		scene.setFill(Color.BLACK);

		angularLabel=new Label();
		linearLabel=new Label();
		joy_status=new Label();
		odom_status=new Label();
		angularLabel.setFont(Font.font(15));
		linearLabel.setFont(Font.font(15));
		joy_status.setFont(Font.font(15));
		odom_status.setFont(Font.font(15));
		setLabel(linearLabel, "0", Color.WHITE);
		setLabel(angularLabel, "0", Color.WHITE);
		setLabel(joy_status, "No Controller", Color.RED);
		setLabel(odom_status, "No Odometry", Color.RED);
		
		linearLabel.relocate(0, width/2-20);
		linearLabel.setMinWidth(width);
		linearLabel.setAlignment(Pos.CENTER);
		
		angularLabel.relocate(0, width/2+20);
		angularLabel.setMinWidth(width);
		angularLabel.setAlignment(Pos.CENTER);
		
		joy_status.relocate(0, height-20);
		joy_status.setMinWidth(width);
		joy_status.setAlignment(Pos.CENTER);
		
		odom_status.relocate(0, height-40);
		odom_status.setMinWidth(width);
		odom_status.setAlignment(Pos.CENTER);
		
		
		arrowW=new Polygon(arrowSize, 0, -arrowSize, 0 ,0, -arrowSize);
		arrowA=new Polygon(0, arrowSize, 0, -arrowSize ,-arrowSize, 0);
		arrowS=new Polygon(arrowSize, 0, -arrowSize, 0 ,0, arrowSize);
		arrowD=new Polygon(0, arrowSize, 0, -arrowSize ,arrowSize, 0);
		arrowW.relocate(width/2-arrowSize, width/2-arrowSize*4);
		arrowW.setFill(Color.GRAY);
		arrowA.relocate(width/2-arrowSize*4, width/2-arrowSize);
		arrowA.setFill(Color.GRAY);
		arrowS.relocate(width/2-arrowSize, width/2+arrowSize*3);
		arrowS.setFill(Color.GRAY);
		arrowD.relocate(width/2+arrowSize*3, width/2-arrowSize);
		arrowD.setFill(Color.GRAY);
		
		
		root.getChildren().addAll(angularLabel, linearLabel, joy_status, odom_status, arrowW, arrowA, arrowS, arrowD);
		panel.setScene(scene);
		frame.setBounds(0, 0, width, height);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		scene.addEventHandler(KeyEvent.KEY_PRESSED, (event)->{
			switch (event.getCode()) {
			case W:
				linear=linearVelocity;
				arrowW.setFill(Color.RED);
				break;
			case A:
				angular=angularVelocity;
				arrowA.setFill(Color.RED);
				break;
			case S:
				linear=-linearVelocity;
				arrowS.setFill(Color.RED);
				break;
			case D:
				angular=-angularVelocity;
				arrowD.setFill(Color.RED);
				break;
			case C:
				System.exit(0);
				break;
			default:
				break;
			}
		});
		scene.addEventHandler(KeyEvent.KEY_RELEASED, (event)->{
			switch (event.getCode()) {
			case W:
				linear=0;
				arrowW.setFill(Color.GRAY);
				break;
			case A:
				angular=0;
				arrowA.setFill(Color.GRAY);
				break;
			case S:
				linear=0;
				arrowS.setFill(Color.GRAY);
				break;
			case D:
				angular=0;
				arrowD.setFill(Color.GRAY);
				break;
			default:
				break;
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
			if(!isJoy) {isJoy=true; setLabel(joy_status, "Connected Controller", Color.LAWNGREEN);}
			this.joy=(Joy)message;
			float[] axes=((Joy)message).getAxes();
			angular=((axes[3]-1)*-1)/2+(axes[4]-1)/2;
			linear=(axes[1]+axes[5])/2;
			if(Math.abs(angular)<0.01) {angular=0;}
			if(Math.abs(linear)<0.01) {linear=0;}
		});
		Subscriber odom_subscriber=new Subscriber("odom", Odometry._TYPE);
		odom_subscriber.addMessageListener((message)->{
			if(!isOdom) {isOdom=true; setLabel(odom_status, "Connected Odometry", Color.LAWNGREEN);}
			Twist twist=((Odometry)message).getTwist().getTwist();
			odom_angular=twist.getAngular().getZ();
			odom_linear=twist.getLinear().getX();
			if(Math.abs(odom_angular)<0.01) {odom_angular=0;}
			if(Math.abs(odom_linear)<0.01) {odom_linear=0;}
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
						linearLabel.setText(format(linear)+" -> "+format(odom_linear));
						angularLabel.setText(format(angular)+" -> "+format(odom_angular));
						if(isJoy) {
							arrowW.setFill(Color.GRAY);
							arrowA.setFill(Color.GRAY);
							arrowS.setFill(Color.GRAY);
							arrowD.setFill(Color.GRAY);
							if(joy.getAxes()[1]>0||joy.getAxes()[5]>0) {
								arrowW.setFill(Color.RED);
							}
							if(joy.getAxes()[1]<0||joy.getAxes()[5]<0) {
								arrowS.setFill(Color.RED);
							}
							if(joy.getAxes()[3]<1) {
								arrowA.setFill(Color.RED);
							}
							if(joy.getAxes()[4]<1) {
								arrowD.setFill(Color.RED);
							}
						}
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
	
	private String format(Object obj) {
		return String.format("%.2f", obj);
	}
	
	
}
