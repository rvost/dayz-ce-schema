package io.github.rvost.lemminx.dayz.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.rvost.lemminx.dayz.model.GlobalsValidationError.GlobalsValidationErrorCode.TYPE_MISMATCH;
import static io.github.rvost.lemminx.dayz.model.GlobalsValidationError.GlobalsValidationErrorCode.VALUE_OUT_OF_RANGE;

// TODO: Consider alternative implementations
public enum GlobalsVariable {
    AnimalMaxCount("0", GlobalsVariable::validateInteger),
    CleanupAvoidance("0", GlobalsVariable::validateInteger),
    CleanupLifetimeDeadAnimal("0", GlobalsVariable::validateInteger),
    CleanupLifetimeDeadInfected("0", GlobalsVariable::validateInteger),
    CleanupLifetimeDeadPlayer("0", GlobalsVariable::validateInteger),
    CleanupLifetimeDefault("0", GlobalsVariable::validateInteger),
    CleanupLifetimeLimit("0", GlobalsVariable::validateInteger),
    CleanupLifetimeRuined("0", GlobalsVariable::validateInteger),
    FlagRefreshFrequency("0", GlobalsVariable::validateInteger),
    FlagRefreshMaxDuration("0", GlobalsVariable::validateInteger),
    FoodDecay("0", GlobalsVariable::validateFlag),
    IdleModeCountdown("0", GlobalsVariable::validateInteger),
    IdleModeStartup("0", GlobalsVariable::validateFlag),
    InitialSpawn("0", v -> validateIntegerWithRange(v, 0, 100)),
    LootDamageMax("1", v -> validateFloatWithRange(v, 0, 1)),
    LootDamageMin("1", v -> validateFloatWithRange(v, 0, 1)),
    LootProxyPlacement("0", GlobalsVariable::validateInteger),
    LootSpawnAvoidance("0", GlobalsVariable::validateInteger),
    RespawnAttempt("0", GlobalsVariable::validateInteger),
    RespawnLimit("0", GlobalsVariable::validateInteger),
    RespawnTypes("0", GlobalsVariable::validateInteger),
    RestartSpawn("0", v -> validateIntegerWithRange(v, 0, 100)),
    SpawnInitial("0", GlobalsVariable::validateInteger),
    TimeHopping("0", GlobalsVariable::validateInteger),
    TimeLogin("0", v -> validateIntegerWithRange(v, 0, 65536)),
    TimeLogout("0", v -> validateIntegerWithRange(v, 0, 65536)),
    TimePenalty("0", GlobalsVariable::validateInteger),
    WorldWetTempUpdate("0", GlobalsVariable::validateFlag),
    ZombieMaxCount("0", GlobalsVariable::validateInteger),
    ZoneSpawnDist("0", GlobalsVariable::validateInteger);

    public final String typeCode;
    private final List<Function<String, Optional<GlobalsValidationError>>> validators;

    GlobalsVariable(String typeCode, Function<String, Optional<GlobalsValidationError>>... validators) {
        this.typeCode = typeCode;
        this.validators = validators != null ? List.of(validators) : List.of();
    }

    public Stream<GlobalsValidationError> validate(String value) {
        return validators.stream()
                .map(v -> v.apply(value))
                .flatMap(Optional::stream);
    }

    private static Optional<GlobalsValidationError> validateInteger(String value) {
        try {
            var r = Integer.parseInt(value);
            return Optional.empty();
        } catch (NumberFormatException e) {
            var message = String.format("Value \"%s\" is not an integer.", value);
            return Optional.of(new GlobalsValidationError(TYPE_MISMATCH, message));
        }
    }

    private static Optional<GlobalsValidationError> validateIntegerWithRange(String value, int from, int to) {
        try {
            var r = Integer.parseInt(value);
            if (r < from || r > to) {
                var message = String.format("Value %d is outside range %d..%d.", r, from, to);
                return Optional.of(new GlobalsValidationError(VALUE_OUT_OF_RANGE, message));
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            var message = String.format("Value \"%s\" is not an integer.", value);
            return Optional.of(new GlobalsValidationError(TYPE_MISMATCH, message));
        }
    }

    private static Optional<GlobalsValidationError> validateFloatWithRange(String value, float from, float to) {
        try {
            var r = Float.parseFloat(value);
            if (r < from || r > to) {
                var message = String.format("Value %.2f is outside range %2.1f..%2.1f.", r, from, to);
                return Optional.of(new GlobalsValidationError(VALUE_OUT_OF_RANGE, message));
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            var message = String.format("Value \"%s\" is not a float point number.", value);
            return Optional.of(new GlobalsValidationError(TYPE_MISMATCH, message));
        }
    }

    private static Optional<GlobalsValidationError> validateFlag(String value) {
        if ("0".equals(value) || "1".equals(value)) {
            return Optional.empty();
        } else {
            return Optional.of(new GlobalsValidationError(TYPE_MISMATCH, "Value must be either \"0\" or \"1\"."));
        }
    }
}
