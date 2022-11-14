package simulation.actor.view;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import simulation.basic.P2d;
import simulation.gui.SimulationView;

import java.util.List;

public class ViewActor extends AbstractBehavior<ViewActor.UpdateViewMsg> {
    public record UpdateViewMsg(List<P2d> positions, double vt, long iter){}

    private final SimulationView viewer;

    private ViewActor(ActorContext<UpdateViewMsg> context, SimulationView viewer) {
        super(context);
        this.viewer = viewer;
    }

    public static Behavior<UpdateViewMsg> create(SimulationView viewer){
        return Behaviors.setup(ctx -> new ViewActor(ctx, viewer));
    }

    @Override
    public Receive<UpdateViewMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(UpdateViewMsg.class, this::onUpdateViewMsg)
                .build();
    }

    private Behavior<UpdateViewMsg> onUpdateViewMsg(UpdateViewMsg message) {
        this.viewer.display(message.positions, message.vt, message.iter);
        return this;
    }
}
