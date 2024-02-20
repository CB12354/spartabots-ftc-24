package org.firstinspires.ftc.teamcode;
//LT RT Strafe
//Tank stick controls
//LB carousel controls
//
//
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import java.util.Arrays;
import java.util.ArrayList;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.LED;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name="TM EL With Hook", group="0Final")
public class TeleOpTankMecanum_Copy3 extends OpMode {
    DcMotor frontleft, frontright, backleft, backright, armhoz, armro, feeder, lift, eps;
    CRServo spin, spin2;
    Servo liftrot, plane, hangswing;
    DistanceSensor dist, distl, distr;
    public int liftpos = 0;
    public float x, y, z, w, pwr, pwr2, leftstick, rightstick;
    public static double deadzone = 0.2;
    public  static double power = 1;
    public static String input = "";
    public static boolean acceptInput;
    public static boolean pos_servo, feedon, planelaunch, hangstate, pixelMode;
    public static boolean lstop, rstop = false;
    public static boolean pixelModeFinish = true;
    public static double speedmult = 0.6;
    public static double default_speed = 0.75;
    public static int iterate = 0;
    public static boolean acceptDirection;
    
    public DigitalChannel red, grn;
    public static String speedString;
    //Priming variables because while loops don't work
    public static boolean primeAcceptInput = false;
    public static boolean primeDisableInput = false;
    public static String primeButton = "";
    
    public void buttonAction() {
        if (buttonRest()) {
            if (primeButton.equals("a")) {
                pos_servo = !pos_servo;
                sleeptime(100);
                telemetry.addData("Lift Servo!", pos_servo);
                telemetry.update();
                
            }
            if (primeButton.equals("b")) {
                if (speedmult == 0.8) {
                  speedmult = 0.6;
                  spin.setPower(0.1);
                   telemetry.addData("Slow mode!", "");
                   telemetry.update();
               } else {
                   speedmult = 0.8;
                   spin.setPower(0.6);
                   telemetry.addData("Speedyboi mode!", "");
                   telemetry.update();
                }
            }
            if (primeButton.equals("x")) {
                feedon = !feedon;
            }
            primeButton = "";
        }
        
    }
    public void sleeptime(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            
        }
    }
    public void liftposval(int add) {
        if (liftpos + add > 0) {
            liftpos = 0;
        } else if (liftpos + add < -3000) {
            liftpos = -3000;
        }
         else {
            liftpos += add;
        }
        
        
    }
    public double calcspeed(int multiplier) {
        return multiplier*default_speed*speedmult;
        
    }
    public boolean dpadRest() {
        if (!(gamepad1.dpad_up || gamepad1.dpad_down || gamepad1.dpad_right || gamepad1.dpad_left)) {
            return true;
        }
        return false;
    }
    public boolean buttonRest() {
        if (!(gamepad1.a || gamepad1.b || gamepad1.x || gamepad1.y)) {
            return true;
        }
        return false;
    }
    public boolean allRest() {
        if (dpadRest() && buttonRest()) {
            return true;
        }
        return false;
    }
    public boolean waitAllRest(boolean resting) {
        
        if (allRest()) {
            resting = true;
        }
        if (!resting) {
            waitAllRest(false);
        }
        return resting;
    }
    public boolean inRange(double check, double target, double rangeud) {
        if (check > (target - rangeud) && check < (target + rangeud)) {
            return true;
        }
        return false;
    }
    
    @Override
    public void init() {
        frontleft = hardwareMap.dcMotor.get("fl");
        frontright = hardwareMap.dcMotor.get("fr");
        backleft = hardwareMap.dcMotor.get("bl");
        backright = hardwareMap.dcMotor.get("br");
        feeder = hardwareMap.dcMotor.get("feed");
        lift = hardwareMap.dcMotor.get("lift");
        //eps = hardwareMap.dcMotor.get("extrude");
        pixelMode = false;
        hangswing = hardwareMap.servo.get("hangswing");
        lift.setTargetPosition(0);
        //eps.setTargetPosition(0);
        liftrot = hardwareMap.servo.get("liftrot");
        plane = hardwareMap.servo.get("plane");
        hangstate = false;
        spin = hardwareMap.crservo.get("spin");
        red = hardwareMap.get(DigitalChannel.class, "red");
        grn = hardwareMap.get(DigitalChannel.class, "grn");
        //inputfailed = true;
        red.setMode(DigitalChannel.Mode.OUTPUT);
        grn.setMode(DigitalChannel.Mode.OUTPUT);
        
        //spin2 = hardwareMap.crservo.get("spin2");
        pos_servo = false;
        feedon = false;
        planelaunch = false;
        dist = hardwareMap.get(DistanceSensor.class, "distbr");
        distl = hardwareMap.get(DistanceSensor.class, "distbl");
        distr = hardwareMap.get(DistanceSensor.class, "distr");
        
        //carousel = hardwareMap.dcMotor.get("cs");
       // armhoz = hardwareMap.dcMotor.get("armhoz"); // this is the arm that move on a horizontal axis
        //armro = hardwareMap.dcMotor.get("armro"); //this is the arm that rotates
        //arm = hardwareMap.servo.get("arm"); //this is the servo that grabs
        //arm2 = hardwareMap.servo.get("arm2"); // this is the one that grabs too f there is two servos then uncomment this but the first comment slash

        frontright.setDirection(DcMotor.Direction.REVERSE);
        backright.setDirection(DcMotor.Direction.REVERSE);
        backleft.setDirection(DcMotor.Direction.FORWARD);
        frontleft.setDirection(DcMotor.Direction.FORWARD);
        feeder.setDirection(DcMotor.Direction.REVERSE);
        liftrot.setDirection(Servo.Direction.FORWARD);
        //plane.setDirection(Servo.Direction.FORWARD);
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        //eps.setDirection(DcMotor.Direction.REVERSE);
        //armhoz.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        //
        // armro.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
       // arm.setPosition(1);
      //  arm2.setPosition(0); // Uncomment this is there is two servo for grab

    }
/*  THIS IS WHERE INPUT COMMANDS GO
    YOU CAN PUT ANY LIST OF COMMANDS HERE
    FORMAT:
    |DIRECTION DIRECTION DIRECTION |
    NOTE THAT THERE MUST BE A SPACE AFTER EVERY DIRECTION INCLUDING THE LAST ONE!
*/

/*  Current command list:
    Y-Up-Y: Launch drone
    Y-Up-Up-Y: Hang the bot from the truss
    Y-Left-Y: Automatically line up the robot and dump a pixel onto the canvas
    Y-Up-Up-Down-Down-Left-Right-Left-Right-Y: Increases speed to insane levels. Be careful when using this!
*/
    public static final String[] CMDLSTARRAY = {"UP ", "UP UP ", "LEFT ", "UP UP DOWN DOWN LEFT RIGHT LEFT RIGHT "};
    public static final List<String> CMD_LST = Arrays.asList(CMDLSTARRAY);
    public static final String[] CMDFUNCARRAY = {"Launch", "Switch Lift/Hanger Controls", "Auto Pixel", "Warp Drive"};
    public static final List<String> CMDFUNC_LST = Arrays.asList(CMDFUNCARRAY);
    public static String searchInput() {
        for (int i = 0; i < CMD_LST.size(); i++) {
            if (input.equals(CMD_LST.get(i))) {
                return CMDFUNC_LST.get(i);
            }
        }
        return "None";
    }
    public static void interpretInput(String in) {
        if (in.equals("UP ")) {
            planelaunch = !planelaunch;
        }
        if (in.equals("UP UP ")) {
            //Hang code goes here
            /*
            if (!hangstate) {
                hangstate = true;
            } else {
                hangstate = false;
            }
            */
            
        }
        if (in.equals("LEFT ")) {
            if (!pixelMode) {
                pixelMode = true;
                lstop = false;
                rstop = false;
                pixelModeFinish = false;
            } else {
                pixelMode = false;
                pixelModeFinish = true;
            }
            
        }
        if (in.equals("UP UP DOWN DOWN LEFT RIGHT LEFT RIGHT ")) {
            speedmult = 3;
        }
        input = "";
    }
    @Override
    public void loop() {
        double amountofpowerforarmro, amountofpowerforarmhoz;
        getJoyVals();
        //updates joyvalues with deadzones, xyzw
        pwr = y; //this can be tweaked for exponential power increase
        pwr2 = w;
        amountofpowerforarmro = .5; // Change this if you w
        // ant to increase or decrease the power for the amount of power for armro
        amountofpowerforarmhoz = 1; // Change this if you want to increase or decrese the power for the amount of power for armhoz
        lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        if (!pixelMode) {
            frontleft.setPower(Range.clip(pwr, calcspeed(-1), calcspeed(1))); // from frontright - x - z
    backright.setPower(Range.clip(pwr2, calcspeed(-1), calcspeed(1))); //from backleft - x + z
    frontright.setPower(Range.clip(pwr2, calcspeed(-1), calcspeed(1))); //from frontleft + x + z
    backleft.setPower(Range.clip(pwr, calcspeed(-1), calcspeed(1))); //from backright + x - z
        }
       
    if (gamepad1.left_trigger > .1 && !hangstate) {
        liftposval((int)(gamepad1.left_trigger*300));
    } else if (gamepad1.right_trigger < .1 && !pixelMode) {
        liftpos = lift.getCurrentPosition();
    }
    if (gamepad1.right_trigger > .1 && !hangstate) {
        liftposval((int)(-1*gamepad1.right_trigger*300));
    } else if (gamepad1.left_trigger < .1 && !pixelMode) {
        liftpos = lift.getCurrentPosition();
    }
    /*
    if (gamepad1.right_trigger > 0.1 && hangstate) {
        //eps.setPower(-2);
    } else if (gamepad1.left_trigger > 0.1 && hangstate) {
        //eps.setPower(2);
    } else {
        //eps.setPower(0);
    }*/
    telemetry.addData("liftpos", liftpos);
    telemetry.addData("actual lift loc", lift.getCurrentPosition());
    telemetry.addData("target loc", lift.getTargetPosition());
    //telemetry.addData("hang state", hangstate);
    telemetry.addData("Left dist", distl.getDistance(DistanceUnit.INCH));
    telemetry.addData("Right dist", dist.getDistance(DistanceUnit.INCH));
    if (pixelMode) {
        
        telemetry.addData("Left stopped", lstop);
        telemetry.addData("Right stopped", rstop);
    }
    /*
    if (hangstate) {
        telemetry.addData("Position of Hanger", eps.getCurrentPosition());
        telemetry.addData("Target position of hanger", eps.getTargetPosition());
    }
    */
    telemetry.addData("Accepting input?", acceptInput);
    if (acceptInput) {
        telemetry.addData("Current input value", input);
        telemetry.addData("Input matches command", searchInput());
    }
    
    
    switch ((int) speedmult) {
        case 0:
            speedString = "Slow";
            break;
        case 1:
            speedString = "Normal";
            break;
        case 3:
            speedString = "HYPERSPEEDO";
            break;
        default:
            speedString = "huh?";
            break;
    }
    
    //INPUT SECTION
    //Detects inputs on dpad after 
    if (gamepad1.y && primeButton.equals("")) {
        if (acceptInput) {
            primeDisableInput = true;
        } else {
            primeAcceptInput = true;
        }
        primeButton = "y";
    }
    if (buttonRest() && primeAcceptInput) {
        acceptInput = true;
        input = "";
        primeAcceptInput = false;
    }
    if (buttonRest() && primeDisableInput) {
        acceptInput = false;
        primeDisableInput = false;
        interpretInput(input);
    }
    if (acceptInput) {
        if (gamepad1.dpad_right && acceptDirection) {
            input += "RIGHT ";
            acceptDirection = false;
        }
        if (gamepad1.dpad_up && acceptDirection) {
            input += "UP ";
            acceptDirection = false;
        }
        if (gamepad1.dpad_left && acceptDirection) {
            input += "LEFT ";
            acceptDirection = false;
        }
        if (gamepad1.dpad_down && acceptDirection) {
            input += "DOWN ";
            acceptDirection = false;
        }
        if (dpadRest()) {
            acceptDirection = true;
        }
    }
    
    
    
    
    
    telemetry.addData("Speed", speedString);
    
    telemetry.update();
    

    if (gamepad1.left_bumper) {
        frontleft.setPower(calcspeed(1));
        backright.setPower(calcspeed(-1));
        frontright.setPower(calcspeed(1));
        backleft.setPower(calcspeed(-1));
    }
    if (gamepad1.right_bumper) {
        frontleft.setPower(calcspeed(-1));
        backright.setPower(calcspeed(1));
        frontright.setPower(calcspeed(-1));
        backleft.setPower(calcspeed(1));
    }
    double distpos = dist.getDistance(DistanceUnit.INCH);
    if (distpos < 12.5 && distpos > 8 ) {
        grn.setState(true);
        red.setState(false);
    }
    if (distpos < 8 || distpos > 12.5) {
        grn.setState(false);
        red.setState(true);
    }
    
    
    
    if (gamepad1.a && primeButton.equals("")) {
        primeButton = "a";
        
    }
    if (gamepad1.b && primeButton.equals("")) {
        primeButton = "b";
    }
    if (gamepad1.x && primeButton.equals("")) {
        primeButton = "x";
    
    }
    if (planelaunch == true) {
        plane.setPosition(0);
        sleeptime(2000);
        planelaunch = false;
    } else {
        plane.setPosition(0.65);
        
        
    }
    if (feedon) { 
        feeder.setPower(-1);
    } else {
        feeder.setPower(0);
    }
    if (pos_servo == true && (lift.getCurrentPosition() < -2000)) {
        liftrot.setPosition(0.65);
    } else if (pos_servo == true  && (lift.getCurrentPosition() > -750) && lift.getCurrentPosition() < -50) {
        liftrot.setPosition(0.79);
    } else if (pos_servo = true && lift.getCurrentPosition() < -500 && lift.getCurrentPosition() > -2000) {
        pos_servo = false;
    } else if (!hangstate) {
        liftrot.setPosition(.835);
        //hangswing.setPosition(0);
    } else if (hangstate && pos_servo == true) {
        //hangswing.setPosition(0.25);
    } else {
        //hangswing.setPosition(0);
    }
    
    if (pixelMode) {
        
        double dldist = distl.getDistance(DistanceUnit.INCH);
        double drdist = dist.getDistance(DistanceUnit.INCH);
        double distmax = 8.5;
        double distmin = 6;
        
        if (dldist < distmin) {
            backleft.setPower(-0.25);
            frontleft.setPower(-0.25);
            lstop = false;
        } else if (dldist > distmax) {
            backleft.setPower(0.25);
            frontleft.setPower(0.25);
            lstop = false;
        } else {
            backleft.setPower(0);
            frontleft.setPower(0);
            lstop = true;
        }
        if (drdist < distmin) {
            backright.setPower(-0.25);
            frontright.setPower(-0.25);
            rstop = false;
        } else if (drdist > distmax) {
            backright.setPower(0.25);
            frontright.setPower(0.25);
            rstop = false;
        } else {
            backright.setPower(0);
            frontright.setPower(0);
            rstop = true;
        }
        if (rstop && lstop && !pixelModeFinish) {
            liftpos = -2500;
            lift.setTargetPosition(liftpos);
            lift.setPower(0.5);
            
            if (inRange(lift.getCurrentPosition(), liftpos, 10)) {
                primeButton = "a";
                pixelModeFinish = true;
            }
        } else if (pixelModeFinish) {
            sleeptime(1000);
            primeButton = "a";
            pixelMode = false;
        } else {
            lift.setTargetPosition(lift.getCurrentPosition());
        }
        
        
    }
    
    buttonAction();
    if (liftpos > 0) liftpos = 0;
    lift.setTargetPosition(liftpos);
    lift.setPower(0.5);
    telemetry.update();
    sleeptime(2);
    }
    
    
    public void getJoyVals()
    {
        y = gamepad1.left_stick_y;
        
        x = gamepad1.left_stick_x;
        z = gamepad1.right_stick_x;
        w = gamepad1.right_stick_y;
            leftstick = gamepad2.left_stick_y;
            rightstick = gamepad2.right_stick_y;
        
        
        //updates joystick values
 
        if(Math.abs(x)<deadzone) x = 0;
        if(Math.abs(y)<deadzone) y = 0;
        if(Math.abs(z)<deadzone) z = 0;
        if(Math.abs(w)<0.9) w = 0;
        if(Math.abs(rightstick)<deadzone) rightstick = 0;
        if(Math.abs(leftstick)<deadzone) leftstick = 0;
        //checks deadzones
    }
}
