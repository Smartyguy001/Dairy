//package org.firstinspires.ftc.teamcode.examples.calcified;
//
//import static dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedIMUKt.fromImuOrientationOnRobot;
//import static dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedIMUKt.fromYawPitchRollAngles;
//
//import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
//import com.qualcomm.robotcore.eventloop.opmode.OpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.LynxModuleImuType;
//import com.qualcomm.robotcore.hardware.PwmControl;
//
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
//
//import dev.frozenmilk.dairy.calcified.Calcified;
//import dev.frozenmilk.dairy.calcified.hardware.encoder.AngleEncoder;
//import dev.frozenmilk.dairy.calcified.hardware.encoder.DistanceEncoder;
//import dev.frozenmilk.dairy.calcified.hardware.encoder.TicksEncoder;
//import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedMotor;
//import dev.frozenmilk.dairy.calcified.hardware.motor.Direction;
//import dev.frozenmilk.dairy.calcified.hardware.motor.MotorGroup;
//import dev.frozenmilk.dairy.calcified.hardware.motor.ZeroPowerBehaviour;
//import dev.frozenmilk.dairy.calcified.hardware.pwm.CalcifiedContinuousServo;
//import dev.frozenmilk.dairy.calcified.hardware.pwm.CalcifiedServo;
//import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedAnalogInput;
//import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedDigitalInput;
//import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedDigitalOutput;
//import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedIMU;
//import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedIMUKt;
//import dev.frozenmilk.dairy.core.FeatureRegistrar;
//import dev.frozenmilk.dairy.core.util.OpModeLazyCell;
//import dev.frozenmilk.util.units.current.Current;
//import dev.frozenmilk.util.units.current.CurrentUnits;
//import dev.frozenmilk.dairy.core.util.supplier.logical.Conditional;
//import dev.frozenmilk.dairy.core.util.supplier.logical.EnhancedBooleanSupplier;
//import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents;
//import dev.frozenmilk.util.units.angle.Angle;
//import dev.frozenmilk.util.units.angle.AngleUnits;
//import dev.frozenmilk.util.units.angle.Wrapping;
//import dev.frozenmilk.util.units.distance.Distance;
//import dev.frozenmilk.util.units.distance.DistanceUnit;
//import dev.frozenmilk.util.units.distance.DistanceUnits;
//import dev.frozenmilk.util.units.orientation.AngleBasedRobotOrientation;
//
//@TeleOp
//@Calcified.Attach( // attaches the Calcified feature
//		automatedCacheHandling = true // these are settings for the feature that we can set
//)
//public class JavaOverview extends OpMode {
//	// This overview will look into using Calcified,
//	// while also covering many features from the Util
//	// and Core components of the Dairy Ecosystem.
//	// This is due to the fact that Calcified and its hardware APIs
//	// make use of these features, and so demonstrating the implementation
//	// of them at the same time is effective and easier than coming up with
//	// more imaginary scenarios for demonstrating the Utilities
//
//	//
//	// Topics:
//	//
//	// OpModeLazyCells
//	// Accessing Features
//	// Accessing Hardware
//	// Motors
//	// Encoders
//	// > Units + NumericSuppliers system
//	// Servos
//	// IMUs
//	// Analog Sensors
//	// Digital Sensors
//	// Conditional + BooleanSuppliers system
//
//	//
//	// OpModeLazyCells
//	//
//	// OpModeLazyCells are designed to be used in an OpMode
//	// they are a delayed eager evaluation system, that work best when writing in Kotlin,
//	// but are still very handy in Java
//	// Additionally, they better group hardware initialisation behaviour and similar
//	// NOTE: OpModeLazyCells automatically deregister themselves at the end of an OpMode
//	// if you want to reuse them, OpModeFreshLazyCell exists
//	// or, you can manually re-register the cell at the start of an OpMode
//	// this should not prove an issue under normal operation
//
//	// this OpModeLazyCell causes the motor in port 0 to be retrieved at the start of init
//	OpModeLazyCell<CalcifiedMotor> motor0 = new OpModeLazyCell<>(() -> Calcified.getControlHub().getMotor(0));
//	// this gets the motor in port 1, and sets the direction to reverse, and then returns it
//	// which is once again evaluated at the start of init
//	OpModeLazyCell<CalcifiedMotor> motor1 = new OpModeLazyCell<>(() -> {
//		CalcifiedMotor motor = Calcified.getControlHub().getMotor(1);
//		motor.setDirection(Direction.REVERSE);
//		return motor;
//	});
//	// its safe to use values from an OpModeLazyCell in others!
//	// a motor group allows you to control multiple motor-like objects as one!
//	OpModeLazyCell<MotorGroup> motorGroup = new OpModeLazyCell<>(() -> new MotorGroup(motor0.get(), motor1.get()));
//
//	// OpModeLazyCells are part of a family of utilities known as "Cell"s
//	// The remainder of this overview is not aimed at a true implementation for an OpMode
//	// and will not cover more of Cells
//	// The Cell system will be covered in full documentation, but should be usable now if you are willing to read the kdoc
//	// The remainder of the variables will not be declared as fields, as you should have the idea by now
//
//	public JavaOverview() {
//		// checking to see if the features you care about actually activated
//		// can be done using this line:
//		// in this case, it checks that Calcified got attached
//		FeatureRegistrar.checkFeatures(Calcified.INSTANCE);
//		// this block and line do not need to be included, but may be useful in debugging why the Features you wanted are not attached
//	}
//
//	@Override
//	public void init() {
//		// Some Features just use the system to update themselves
//		// OpModeLazyCell is an example
//		// It eagerly evaluates its contents in opmode init
//		// and deregisters its self after opmode ends
//
//		// Other Features are more persistent management systems
//		// These are implemented using the singleton pattern (object in Kotlin)
//		// and they usually need to activated for an OpMode
//		// the @Calcified.Attach annotation lets the FeatureRegistrar (manages features and OpModes)
//		// know that when this OpMode runs, Calcified gets attached, and receives updates from it
//
//		// Calcified provides access to the control and expansion hubs like so:
//		Calcified.getControlHub();
//		Calcified.getExpansionHub();
//		// if one of them isn't plugged in electronically, accessing it will throw an error
//		// hardware is accessed from the hardware maps on the hubs, or from the quick access methods on the hubs themselves
//
//		// from now on, the control hub will be used to show accessing hardware objects, the two hubs are functionally equivalent
//
//		//
//		// Motors
//		//
//		// as shown above, Calcified uses port numbers for accessing hardware objects
//		// no need for a config file!
//		CalcifiedMotor motor2 = Calcified.getControlHub().getMotor(2);
//		// most of the api is sensible and translates from the SDK
//		motor2.getZeroPowerBehaviour();
//		motor2.setZeroPowerBehaviour(ZeroPowerBehaviour.BRAKE);
//		motor2.getDirection();
//		motor2.setDirection(Direction.FORWARD);
//		motor2.getEnabled();
//		motor2.setEnabled(true);
//		motor2.getPower();
//		motor2.setPower(1.0);
//		motor2.getCurrent(); // this is out first look at the units system!
//
//		Current current = motor2.getCurrent();
//		// the units system is a quickly extensible, generic system of immutable families of units
//		// at the moment, we don't know what unit this is in:
//		current.getValue(); // the value of current as a double
//		// we can find out!
//		current.getUnit();
//
//		// this gives us back the equivalent unit in amps, but does not mutate the original variable
//		// if current was already in amps, this is a no-op
//		current.into(CurrentUnits.AMP).getValue(); // now we know that this is in amps!
//
//		// as amps and milli amps are built into the system the following utility methods also exist:
//		current.intoAmps();
//		current.intoMilliAmps();
//
//		// lots of mathematical operations
//		current.plus(current.intoAmps());
//		// and comparison operations
//		current.lessThan(current.intoAmps());
//		// are defined for the families
//
//		// the units system works by 'pulling up'
//		// the right hand side of the operation is converted to match the units of the left hand side of the equation
//
//		// so
//		current.intoMilliAmps().plus(current.intoAmps());
//		// we know that this outputs in MilliAmps
//		// note that this may not be true for non-built-in unit families
//
//		// it is also possible to very easily define your own units for a family
//		// that work alongside predefined ones seamlessly
//
//		// we'll look at that, and the rest of the units system later on
//
//		// determines the level of difference between one value and the next before a write is performed
//		motor2.getCachingTolerance();
//		motor2.setCachingTolerance(0.005); // you might want to lower it for more fine grained control, or raise it for less
//		// the lower it is, the slower your loops will be, and visa versa
//
//		// the over current features are more niche from the sdk, but also supported here
//		motor2.getOverCurrentThreshold();
//		motor2.setOverCurrentThreshold(new Current(CurrentUnits.AMP, 0.2));
//		motor2.getOverCurrent(); // true if current is greater than overCurrentThreshold
//		// over current detection runs much faster than just checking the current, use it if you can
//
//		// and thats it for motors, you might be thinking, what happened to encoders?, or run modes?
//		// Calcified does some major reorganising of ideas here, but offers more powerful alternatives
//		// Calcified works only with the RUN_WITHOUT_ENCODER run mode, as Dairy's Core offers its own way of doing PID controllers
//
//		//
//		// Encoders
//		//
//		// encoders are separate from motors in Calcified
//		// a ticks encoder is probably what you're accustomed to, it just returns the position of the encoder as a Double
//		TicksEncoder encoder = Calcified.getControlHub().getTicksEncoder(0);
//		// separate from the motor direction
//		encoder.getDirection();
//		encoder.setDirection(Direction.FORWARD);
//		// the position
//		encoder.getPosition();
//
//		// the position can be set, this is done software side
//		encoder.setPosition(100.0);
//		// whereas the reset method does the hardware instruction
//		encoder.reset();
//
//		// velocity is taken over a period of the measurementWindow (measured in seconds), which defaults to 20 ms
//		// this is because velocity from encoders can be very noisy
//		encoder.getMeasurementWindow();
//		encoder.setMeasurementWindow(0.02);
//		encoder.getVelocity();
//
//		// raw velocity is not measured over a buffer / window
//		encoder.getRawVelocity();
//
//		// acceleration is the same
//		encoder.getAcceleration();
//		encoder.getRawAcceleration();
//
//		// encoders are part of the unit system
//		// encoders take the form of EnhancedNumberSuppliers
//		// EnhancedSuppliers are all immutable, which means that modifying methods actually produce a new EnhancedSupplier, independent of the previous one
//		// and can be used to easily form conditional binds
//		// more on this later!
//
//		// encoders can be used directly as other unit systems, such as Angles, and Distances
//		AngleEncoder absoluteEncoder = Calcified.getControlHub().getAngleEncoder(1, Wrapping.WRAPPING, 28);
//		DistanceEncoder distanceEncoder = Calcified.getControlHub().getDistanceEncoder(2, DistanceUnits.MILLIMETER, 10);
//		// these work the same as above, but everything is measured as the appropriate reified unit
//
//		// Angles
//		Angle angle = absoluteEncoder.getPosition();
//		// angles can be either wrapping or linear
//		// for an encoder that is set up to be wrapping, it still produces linear angles for velocity and acceleration
//		// wrapping means that an angle exists in the domain of [0, 1] rotations
//		// linear can exist outside of this range
//		// linear can easily be converted down
//
//		// the find error method also exists for units
//		// for angles, this method is important, as if the target is wrapping, then the output will be in the domain [-0.5, 0.5] rotations
//		// and will be corrected to find the shortest distance
//		// this finds the shortest distance from angle to 0
//		angle.findError(new Angle(AngleUnits.RADIAN, Wrapping.WRAPPING, 0));
//
//		// angles have two predefined units, radians and degrees
//
//		// angles also have trig helpers
//		angle.getSin();
//		angle.getCos();
//		angle.getTan();
//
//		// Distance
//		Distance distance = distanceEncoder.getPosition();
//		// distances have 4 predefined units:
//		// meter, millimeter, foot, inch
//
//		// It is likely that you might want to define your own distance unit for the size of your tile
//		// which would be helpful while developing auto, as you can quickly modify the size of a tile when you go to competition
//
//		// units can also be multiplied and divided by doubles
//		distance.div(2);
//		// put to the power of
//		distance.pow(2);
//		distance.sqrt();
//		// or absolute valued
//		distance.abs();
//		// or coerced
//		distance.coerceAtLeast(Distance.NEGATIVE_INFINITY);
//
//		//
//		// Servos
//		//
//		// Servos are fairly simple
//		CalcifiedServo servo = Calcified.getControlHub().getServo(0);
//		// same as the sdk
//		servo.getPosition();
//		servo.setPosition(0);
//		servo.getCachingTolerance();
//		servo.setCachingTolerance(0.01); // tolerance on setting the position, behaves the same as setting motor power
//		servo.getEnabled();
//		servo.setEnabled(true); // enables / disables the port, TODO this may cause both 'linked' servo ports to become disabled, this behaviour has not yet been tested
//		servo.getDirection();
//		servo.setDirection(Direction.FORWARD); // same as the sdk, reverse inverts 0 and 1
//		servo.getPwmRange();
//		servo.setPwmRange(PwmControl.PwmRange.defaultRange); // directly exposes the pwmRange, which can be used to easily change the pwm information, in order to make use of servos that use a different range
//
//		// CR Servos have all the same things, but uses power instead
//		CalcifiedContinuousServo crServo = Calcified.getControlHub().getContinuousServo(1);
//		crServo.getPower();
//		crServo.setPower(0);
//		crServo.getCachingTolerance();
//		crServo.setCachingTolerance(0.01); // tolerance on setting the position, behaves the same as setting motor power
//		crServo.getEnabled();
//		crServo.setEnabled(true); // enables / disables the port, TODO this may cause both 'linked' servo ports to become disabled, this behaviour has not yet been tested
//		crServo.getDirection();
//		crServo.setDirection(Direction.FORWARD); // same as the sdk, reverse inverts 0 and 1
//		crServo.getPwmRange();
//		crServo.setPwmRange(PwmControl.PwmRange.defaultRange); // directly exposes the pwmRange, which can be used to easily change the pwm information, in order to make use of servos that use a different range
//
//		// crServos can be used in a motor group with motors
//		MotorGroup motorGroup = new MotorGroup(motor0.get(), motor1.get(), motor2, crServo);
//		motorGroup.getPower();
//		motorGroup.setPower(1.0);
//
//		//
//		// IMUs
//		//
//		// the imu can be obtained with full defaults
//		CalcifiedIMU imu = Calcified.getControlHub().getIMU();
//		Calcified.getControlHub().getIMU(
//				0, // port defaults to 0
//				LynxModuleImuType.BHI260, // if you don't supply this value, it is automatically detected, which is probably for the best
//				fromImuOrientationOnRobot( // there are lots of ways to generate a starting orientation for the imu!
//						new RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.FORWARD, RevHubOrientationOnRobot.UsbFacingDirection.UP)
//				)
//		);
//		// Dairy uses its own orientation system, but provides high compatibility with the sdk classes for orientation
//		// the following should all be equivalent
//		AngleBasedRobotOrientation orientation = new AngleBasedRobotOrientation(
//				new Angle(AngleUnits.DEGREE, Wrapping.WRAPPING, 90),
//				new Angle(AngleUnits.DEGREE, Wrapping.WRAPPING, 90),
//				new Angle(AngleUnits.DEGREE, Wrapping.WRAPPING, 90)
//		);
//		AngleBasedRobotOrientation orientationFromHubOrientation = fromImuOrientationOnRobot(new RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.FORWARD, RevHubOrientationOnRobot.UsbFacingDirection.UP));
//		AngleBasedRobotOrientation orientationFromYawPitchRollAngles = fromYawPitchRollAngles(new YawPitchRollAngles(AngleUnit.DEGREES, 90.0, 90.0, 90.0, 0));
//
//		// and the opposite can also be done
//		CalcifiedIMUKt.toYawPitchRoll(orientation).getYaw(AngleUnit.DEGREES);
//
//		// but the Dairy orientation system provides support through the angle units we looked at previously
//		// there are sugar shortcuts for heading (zRot)
//		imu.getHeading();
//		imu.getOrientation().getXRot();
//		imu.getOrientation().getYRot();
//		imu.getOrientation().getZRot();
//		// the enhanced supplier implementations for the axes
//		imu.getHeadingSupplier();
//		imu.getXRotSupplier();
//		imu.getYRotSupplier();
//		imu.getZRotSupplier();
//
//		// nice and easy!
//		// also, the heading and orientation be set
//		imu.setHeading(angle);
//		imu.setOrientation(orientationFromYawPitchRollAngles);
//
//		//
//		// Analog Sensors
//		//
//		// another enhanced supplier!
//		// this one is just doubles
//		CalcifiedAnalogInput aInput = Calcified.getControlHub().getAnalogInput(0);
//		// nothing new...
//		aInput.getPosition();
//		aInput.getVelocity();
//		aInput.getRawVelocity();
//		// ...
//
//		//
//		// Digital Sensors
//		//
//		// digital inputs are EnhancedBooleanSuppliers
//		// they are different to the EnhancedNumberSuppliers we have seen so far
//		CalcifiedDigitalInput dInput = Calcified.getControlHub().getDigitalInput(0);
//		dInput.state(); // true / false
//		// rising and falling edge detection
//		dInput.onTrue();
//		dInput.onFalse();
//
//		// debouncing can be applied
//		// remember that these are immutable, so each new EnhancedBooleanSupplier is independent from the others
//		dInput.debounce(0.05);
//		dInput.debounce(0.05, 0.0);
//		dInput.debounceRisingEdge(0.05);
//		dInput.debounceFallingEdge(0.05);
//
//		// the suppliers can be combined
//		dInput.and(() -> true);
//		dInput.or(() -> false);
//		dInput.xor(() -> true);
//
//		// or inverted!
//		dInput.not();
//
//		// dOutputs are pretty boring, they just accept a variable
//		CalcifiedDigitalOutput dOutput = Calcified.getControlHub().getDigitalOutput(1);
//		dOutput.accept(true);
//
//		//
//		// Conditionals
//		//
//		// the conditionals system makes it easy to build EnhancedBooleanSuppliers from EnhancedNumberSuppliers
//		EnhancedBooleanSupplier simpleBinding = encoder.conditionalBindPosition()
//				.greaterThan(0.0)
//				.lessThan(100.0)
//				.bind();
//
//		simpleBinding.state();
//		simpleBinding.onTrue();
//
//		// this system is very intuitive, and binding works best if you go in order of smallest to largest, and it can be very complex
//		EnhancedBooleanSupplier complexBinding = encoder.conditionalBindVelocity()
//				.greaterThanEqualTo(-100.0)
//				.lessThan(-10.0)
//				.greaterThan(200.0)
//				.bind();
//		// this more complex binding will return true if the encoder has a velocity in the following domains: [-100, -10), (200, infinity)
//
//		EnhancedBooleanSupplier unitBinding = distanceEncoder.conditionalBindPosition()
//				.greaterThan(new Distance(DistanceUnits.METER, 10.0))
//				.bind();
//
//		// there are conditional binding builders for all components of motion supplied by an EnhancedNumberSupplier
//		encoder.conditionalBindPosition();
//		encoder.conditionalBindVelocity();
//		encoder.conditionalBindVelocityRaw();
//		encoder.conditionalBindAcceleration();
//		encoder.conditionalBindAccelerationRaw();
//
//		// but you can also make your own
//		double startTime = System.nanoTime() / 1e9;
//		Conditional<Double> customTimer = new Conditional<>(() -> (System.nanoTime() / 1e9) - startTime);
//
//		// conditionals can also be reused!
//		EnhancedBooleanSupplier after90 = customTimer.greaterThan(90.0).bind();
//		EnhancedBooleanSupplier pre30 = customTimer.lessThan(30.0).bind();
//
//		// And that's all!
//
//		// Take a look at controller overview from Core for how to get behaviours like PID
//		// NOTE: Controllers were previously part of Calcified, but were made usable outside of it
//
//		// If you're interested in Gamepad support in a similar vein, checkout the Pasteurized overview
//		// It explains some of the topics around the EnhancedSupplier family as well, so you may find some of what it explains, helps you to understand this
//	}
//
//	private final OpModeLazyCell<EnhancedBooleanSupplier> encoderInRangeCell = new OpModeLazyCell<>(() -> {
//		DistanceEncoder encoder = Calcified.getControlHub().getDistanceEncoder(0, DistanceUnits.METER, 10.0);
//		return encoder.conditionalBindPosition()
//				.greaterThan(new Distance(DistanceUnits.MILLIMETER, 10.0))
//				.lessThan(new Distance(DistanceUnits.METER, 10.0))
//				.bind();
//	});
//
//	private final OpModeLazyCell<EnhancedBooleanSupplier> encoderInRangeCell2 = new OpModeLazyCell<>(() -> {
//		DistanceEncoder encoder = Calcified.getControlHub().getDistanceEncoder(0, DistanceUnits.METER, 10.0);
//		return new EnhancedBooleanSupplier(() -> {
//			return encoder.getPosition().intoMeters().getValue() > new Distance(DistanceUnits.MILLIMETER, 10.0).intoMeters().getValue() &&
//					encoder.getPosition().intoMeters().getValue() < new Distance(DistanceUnits.METER, 10.0).intoMeters().getValue();
//		});
//	});
//
//	private EnhancedBooleanSupplier encoderInRange() {
//		return encoderInRangeCell.get();
//	}
//	@Override
//	public void loop() {
//		if (encoderInRange().onTrue()) {
//			// do w/e
//		}
//	}
//}