package sheg1_steparm.aquaacrobaticsunofficial.network.datasync;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import sheg1_steparm.aquaacrobaticsunofficial.entity.Pose;

import javax.annotation.Nonnull;

@SuppressWarnings("deprecation")
public class PoseSerializer {
    public static final DataSerializer<Pose> POSE = new DataSerializer<Pose>() {
        public void write(PacketBuffer buf, @Nonnull Pose value) {
            buf.writeEnumValue(value);
        }

        @Nonnull
        public Pose read(PacketBuffer buf) {
            return buf.readEnumValue(Pose.class);
        }

        @Nonnull
        public DataParameter<Pose> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Nonnull
        public Pose copyValue(@Nonnull Pose value) {
            return value;
        }
    };

    static {
        DataSerializers.registerSerializer(POSE);
    }
}