package node.gpio;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.platform.Platform;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.platform.PlatformManager;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;
import com.pi4j.util.ConsoleColor;

import java.util.concurrent.Future;

public class RaspbianGpioOutput {

    
    public static void main(String[] args) throws InterruptedException, PlatformAlreadyAssignedException {

        final Console console = new Console();

        console.title("GPIO Control");

        console.promptForExit();

        final GpioController gpio = GpioFactory.getInstance();

        Pin pin = CommandArgumentParser.getPin(
                RaspiPin.class,    // pin provider class to obtain pin instance from
                RaspiPin.GPIO_07,  // default pin if no pin argument found
                args);             // argument array to search in

        
        final GpioPinDigitalOutput output = gpio.provisionDigitalOutputPin(pin, "GPIO Output", PinState.HIGH);

        output.setShutdownOptions(false, PinState.LOW);

        output.addListener(new GpioPinListenerDigital() {
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
               console.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " +
                        ConsoleColor.conditional(
                                event.getState().isHigh(), // conditional expression
                                ConsoleColor.GREEN,        // positive conditional color
                                ConsoleColor.RED,          // negative conditional color
                                event.getState()));        // text to display
            }
        });

        console.println(" ... Successfully provisioned output pin: " + output.toString());
        console.emptyLine();
        console.box("The GPIO output pin states will cycle HIGH and LOW states now.");
        console.emptyLine();

        console.println("--> [" + output.toString() + "] state was provisioned with state = " +
                ConsoleColor.conditional(
                        output.getState().isHigh(), // conditional expression
                        ConsoleColor.GREEN,         // positive conditional color
                        ConsoleColor.RED,           // negative conditional color
                        output.getState()));        // text to display

        Thread.sleep(500);

        console.emptyLine();
        console.println("Setting output pin state is set to LOW.");
        output.low(); // or ... output.setState(PinState.LOW);

        Thread.sleep(500);

        console.emptyLine();
        console.println("Setting output pin state from LOW to HIGH.");
        output.setState(PinState.HIGH); // or ... output.high();

        Thread.sleep(500);

        console.emptyLine();
        console.println("Toggling output pin state from HIGH to LOW.");
        output.toggle();

        Thread.sleep(500);

        console.emptyLine();
        console.println("Pulsing output pin state HIGH for 1 second.");
        output.pulse(1000, true); // set second argument to 'true' use a blocking call
        Thread.sleep(50);

        console.emptyLine();
        console.println("Blinking output pin state between HIGH and LOW for 3 seconds with a blink rate of 250ms.");
        Future future = output.blink(250, 3000);

        while(!future.isDone()){
            Thread.sleep(50);
        }
        gpio.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);;
        gpio.shutdown();
    }
}