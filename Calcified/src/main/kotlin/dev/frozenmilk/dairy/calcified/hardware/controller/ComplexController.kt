package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.OpModeWrapper
import dev.frozenmilk.util.angle.Angle
import java.util.function.Supplier

interface ComplexController<IN> : Feature {
	var autoUpdate: Boolean
	var target: IN
	val motors: SimpleMotor
	val calculators: List<CalculatorComponent<IN>>
	var previousTime: Long
	var currentTime: Long

	fun deltaTime(): Double {
		return currentTime - previousTime / 1E9
	}

	/**
	 * @param tolerance the total power output from error based controller calculation at which the system is considered at target
	 */
	fun isBusy(tolerance: Double): Boolean {
		return calculators.filterIsInstance<ErrorBasedCalculator<IN>>().sumOf { it.calculate(target, deltaTime()) } < tolerance.coerceIn(0.0, 1.0)
	}

	fun update() {
		if (!autoUpdate) return
		previousTime = currentTime
		currentTime = System.nanoTime()
		motors.power = calculators.sumOf { it.calculate(target, deltaTime()) }
	}

	override fun preUserInitHook(opMode: OpModeWrapper) {}

	override fun postUserInitHook(opMode: OpModeWrapper) {
		update()
	}

	override fun preUserInitLoopHook(opMode: OpModeWrapper) {
	}

	override fun postUserInitLoopHook(opMode: OpModeWrapper) {
		update()
	}

	override fun preUserStartHook(opMode: OpModeWrapper) {
	}

	override fun postUserStartHook(opMode: OpModeWrapper) {
		update()
	}

	override fun preUserLoopHook(opMode: OpModeWrapper) {
	}

	override fun postUserLoopHook(opMode: OpModeWrapper) {
		update()
	}

	override fun preUserStopHook(opMode: OpModeWrapper) {
	}

	override fun postUserStopHook(opMode: OpModeWrapper) {
		motors.power = 0.0
	}
}

interface CalculatorComponent<IN> {
	fun calculate(target: IN, deltaTime: Double): Double
}
abstract class ErrorBasedCalculator<IN>(val errorSupplier: ErrorSupplier<IN, Double>) : CalculatorComponent<IN>

abstract class PositionBasedCalculator<IN>(val positionSupplier: Supplier<IN>) : CalculatorComponent<IN>

class PController<IN>(errorSupplier: ErrorSupplier<IN, Double>, private val kP: Double) : ErrorBasedCalculator<IN>(errorSupplier) {
	override fun calculate(target: IN, deltaTime: Double): Double {
		return errorSupplier.getError(target) * kP
	}
}

class IController<IN>(errorSupplier: ErrorSupplier<IN, Double>, private val kI: Double, private val lowerLimit: Double, private val upperLimit: Double) : ErrorBasedCalculator<IN>(errorSupplier) {
	private var i = 0.0
	override fun calculate(target: IN, deltaTime: Double): Double {
		i += errorSupplier.getError(target) / deltaTime * kI
		i = i.coerceIn(lowerLimit, upperLimit)
		return i
	}
}

class DController<IN>(errorSupplier: ErrorSupplier<IN, Double>, private val kD: Double) : ErrorBasedCalculator<IN>(errorSupplier) {
	private var previousError = 0.0
	override fun calculate(target: IN, deltaTime: Double): Double {
		val error = errorSupplier.getError(target)
		val result = (error - previousError) / deltaTime * kD
		previousError = error
		return result
	}
}