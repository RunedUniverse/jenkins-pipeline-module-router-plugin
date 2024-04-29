package org.jenkinsci.plugins.pipeline.modulerouter.modeldefinition.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringEscapeUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.validator.ModelValidator;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.sf.json.JSONObject;

public class ModelASTStagePerModule extends ModelASTStage {

	private Boolean parallelize;

	public ModelASTStagePerModule(Object sourceLocation) {
		super(sourceLocation);
	}

	@Override
	@NonNull
	public JSONObject toJSON() {
		return super.toJSON().accumulate("parallelize", this.parallelize);
	}

	@Override
	public void validate(ModelValidator validator) {
		validate(validator, this.parallelize);
	}

	@Override
	@NonNull
	public String toGroovy() {
		StringBuilder result = new StringBuilder().append("stage(\'")
				.append(String.join(", ", argsToGroovy()))
				.append("\') {\n")
				.append(blockToGroovy())
				.append("}\n");

		return result.toString();
	}

	protected List<String> argsToGroovy() {
		final List<String> args = new ArrayList<String>(1);
		args.add("name: " + getName().replace("'", "\\'"));
		// additional parameters -- start
		if (this.parallelize != null) {
			args.add("parallelize: " + this.parallelize.toString());
		}
		// additional parameters -- end
		return args;
	}

	// copy block from {@link ModelASTStage.toGroovy()}
	// wouldn't it be great if they migrated this ?!
	protected StringBuilder blockToGroovy() {
		final StringBuilder block = new StringBuilder(super.toGroovy());

		block.append(toGroovy(getStages()));
		if (getParallel() != null || getMatrix() != null) {
			if (getFailFast() != null && getFailFast()) {
				block.append("failFast true\n");
			}
		}
		block.append(toGroovy(getParallel()))
				.append(toGroovy(getMatrix()));

		if (!getBranches().isEmpty()) {
			block.append("steps {\n");
			if (getBranches().size() > 1) {
				block.append("parallel(");
				boolean first = true;
				for (ModelASTBranch branch : getBranches()) {
					if (first) {
						first = false;
					} else {
						block.append(',');
					}
					block.append('\n');
					block.append('"' + StringEscapeUtils.escapeJava(branch.getName()) + '"')
							.append(": {\n")
							.append(branch.toGroovy())
							.append("\n}");
				}
				if (getFailFast() != null && getFailFast()) {
					block.append(",\nfailFast: true");
				}
				block.append("\n)\n");
			} else if (getBranches().size() == 1) {
				block.append(getBranches().get(0)
						.toGroovy());
			}

			block.append("}\n");
		}
		return block;
	}

	public Boolean getParallelize() {
		return parallelize;
	}

	public void setParallelize(Boolean parallelize) {
		this.parallelize = parallelize;
	}

	@Override
	public String toString() {
		return "ModelASTStage{" + "name='" + getName() + '\'' + ", " + super.toString() + ", stages=" + getStages()
				+ ", branches=" + getBranches() + ", failFast=" + getFailFast() + ", parallel=" + getParallel()
				+ ", matrix=" + getMatrix() + ", parallelize=" + this.parallelize + "}";
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		ModelASTStagePerModule that = (ModelASTStagePerModule) o;
		return Objects.equals(getName(), that.getName()) && Objects.equals(getStages(), that.getStages())
				&& Objects.equals(getBranches(), that.getBranches())
				&& Objects.equals(getFailFast(), that.getFailFast())
				&& Objects.equals(getParallel(), that.getParallel()) && Objects.equals(getMatrix(), that.getMatrix())
				&& Objects.equals(getParallelContent(), that.getParallelContent())
				&& Objects.equals(getParallelize(), that.getParallelize());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getParallelize());
	}

}
