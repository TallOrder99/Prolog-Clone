package wam;

import java.util.List;
import java.util.Map;

/**
 * A simple data carrier to hold the results of a compilation.
 * @param code The generated list of WAM instructions.
 * @param labels A map from string labels (e.g., "parent/2") to their integer address in the code list.
 */
public record CompilerResult(List<Instruction> code, Map<String, Integer> labels) {}