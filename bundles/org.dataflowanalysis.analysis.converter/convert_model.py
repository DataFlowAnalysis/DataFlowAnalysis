import argparse
import ast
import json


# For CLI
arg_parser = argparse.ArgumentParser(prog = "convert_model",
                                    description = "Convert Dataflow Diagrams between different formats.")

arg_parser.add_argument("input_path",
                        metavar = "input-path",
                        type = str,
                        help = "Path to the input file.")

arg_parser.add_argument("output_format",
                        metavar = "output-format",
                        type = str,
                        help = "Output format. Options are: [\"json\" (JSON), \"py\" (Python / CodeableModels), \"txt\"(PlantUML)]")

arg_parser.add_argument("-op",
                        metavar = "--output-path",
                        type = str,
                        help = "Path where output file should be stored.")


def plantuml_to_codeable(file_as_lines: str, model_name: str) -> str:
    """Converts PlantUML file into CodeableModels file.
    """

    node = str()

    output_lines = add_header_codeable(model_name)

    for line in file_as_lines:
        if "Process" in line:
            if "database" in line:
                name, stereotypes, tagged_values = parse_node_plantuml(line)
                output_lines = add_database_codeable(output_lines, name, stereotypes, tagged_values)
            else:
                name, stereotypes, tagged_values = parse_node_plantuml(line)
                node = name
                output_lines = add_service_codeable(output_lines, name, stereotypes, tagged_values)

        elif "External Entity" in line:
            name, stereotypes, tagged_values = parse_node_plantuml(line)
            output_lines = add_external_entity_codeable(output_lines, name, stereotypes, tagged_values)

        elif "->" in line:
            sender, receiver, stereotypes, tagged_values = parse_flow_plantuml(line)
            output_lines = add_flow_codeable(output_lines, sender, receiver, stereotypes, tagged_values)

    output_lines = add_footer_codeable(output_lines, node)

    return output_lines


def codeable_to_plantuml(file_as_lines: str) -> str:
    """Converts CodeableModels file into PlantUML file.
    """

    output_lines = add_header_plantuml()

    for line in file_as_lines:
        if "CClass(external_component" in line:
            name, stereotypes, tagged_values = parse_node_codeable(line)
            output_lines = add_external_entity_plantuml(output_lines, name, stereotypes, tagged_values)

        elif "CClass(service" in line:
            name, stereotypes, tagged_values = parse_node_codeable(line)
            output_lines = add_service_plantuml(output_lines, name, stereotypes, tagged_values)

        elif "CClass(database_component" in line:
            name, stereotypes, tagged_values = parse_node_codeable(line)
            output_lines = add_database_plantuml(output_lines, name, stereotypes, tagged_values)

        elif "add_links(" in line:
            sender, receiver, stereotypes, tagged_values = parse_flow_codeable(line)
            output_lines = add_flow_plantuml(output_lines, sender, receiver, stereotypes, tagged_values)

    output_lines = add_footer_plantuml(output_lines)

    return output_lines


def plantuml_to_json(file_as_lines: str) -> str:
    """Converts PlantUML file into JSON file.
    """

    output_dict = dict()

    for line in file_as_lines:
        if "Service" in line:
            name, stereotypes, tagged_values = parse_node_plantuml(line)
            output_dict = add_service_json(output_dict, name, stereotypes, tagged_values)

        elif "External Entity" in line:
            name, stereotypes, tagged_values = parse_node_plantuml(line)
            output_dict = add_external_entity_json(output_dict, name, stereotypes, tagged_values)

        elif "->" in line:
            sender, receiver, stereotypes, tagged_values = parse_flow_plantuml(line)
            output_dict = add_flow_json(output_dict, sender, receiver, stereotypes, tagged_values)

    return output_dict


def json_to_plantuml(input_dict: dict) -> str:
    """Converts JSON file into PlantUML file.
    """

    output_lines = add_header_plantuml()

    if "services" in input_dict:
        services = input_dict["services"]
        for service in services:
            output_lines = add_service_plantuml(output_lines, service["name"], service["stereotypes"], service["tagged_values"])

    if "external_entities" in input_dict:
        external_entities = input_dict["external_entities"]
        for external_entitie in external_entities:
            output_lines = add_external_entity_plantuml(output_lines, external_entitie["name"], external_entitie["stereotypes"], external_entitie["tagged_values"])

    if "information_flows" in input_dict:
        flows = input_dict["information_flows"]
        for flow in flows:
            output_lines = add_flow_plantuml(output_lines, flow["sender"], flow["receiver"], flow["stereotypes"], flow["tagged_values"])

    output_lines = add_footer_plantuml(output_lines)

    return output_lines


def codeable_to_json(file_as_lines: str) -> str:
    """Converts CodeableModels file into JSON file.
    """

    output_dict = dict()

    for line in file_as_lines:
        if "CClass(external_component" in line:
            name, stereotypes, tagged_values = parse_node_codeable(line)
            output_dict = add_external_entity_json(output_dict, name, stereotypes, tagged_values)

        elif "CClass(service" in line or "CClass(database_component" in line:
            name, stereotypes, tagged_values = parse_node_codeable(line)
            output_dict = add_service_json(output_dict, name, stereotypes, tagged_values)

        elif "add_links(" in line:
            sender, receiver, stereotypes, tagged_values = parse_flow_codeable(line)
            output_dict = add_flow_json(output_dict, sender, receiver, stereotypes, tagged_values)

    return output_dict


def json_to_codeable(input_dict: dict, model_name) -> str:
    """Converts JSON file into CodeableModels file.
    """

    output_lines = add_header_codeable(model_name)

    if "services" in input_dict:
        services = input_dict["services"]
        for service in services:
            node = service["name"]
            output_lines = add_service_codeable(output_lines, service["name"], service["stereotypes"], service["tagged_values"])

    if "external_entities" in input_dict:
        external_entities = input_dict["external_entities"]
        for external_entitie in external_entities:
            output_lines = add_external_entity_codeable(output_lines, external_entitie["name"], external_entitie["stereotypes"], external_entitie["tagged_values"])

    if "information_flows" in input_dict:
        flows = input_dict["information_flows"]
        for flow in flows:
            output_lines = add_flow_codeable(output_lines, flow["sender"], flow["receiver"], flow["stereotypes"], flow["tagged_values"])

    output_lines = add_footer_codeable(output_lines, node)

    return output_lines




################ Adding headers / footers

def add_header_plantuml() -> str:
    """Adds PlantUml header to the passed input string.
    """

    output = """
@startuml
skinparam monochrome true
skinparam ClassBackgroundColor White
skinparam defaultFontName Arial
skinparam defaultFontSize 11


digraph dfd2{
    node[shape=record]
"""
    return output


def add_footer_plantuml(input: str) -> str:
    """Adds PlantUml footer to the passed input string.
    """

    output = input + """
}
@enduml
"""
    return output


def add_header_codeable(model_name) -> str:
    """Returns string of CodeableModels header.
    """

    output = "from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute \n\
from metamodels.microservice_dfds_metamodel import * \n\
from plant_uml_renderer import PlantUMLGenerator \n\
plantuml_path = \"./../plantuml.jar\" \n\
output_directory = \".\" \n\
model_name = \"" + model_name + "\""

    return output

def add_footer_codeable(input_string: str, node: str) -> str:
    """Adds footer for CodeableModels to input string.
    """

    output = "\nmodel = CBundle(model_name, elements = " + node + ".class_object.get_connected_elements())\n\
def run():\n\
    generator = PlantUMLGenerator()\n\
    generator.plant_uml_jar_path = plantuml_path\n\
    generator.directory = output_directory\n\
    generator.object_model_renderer.left_to_right = True\n\
    generator.generate_object_models(model_name, [model, {}])\n\
    print(f\"Generated models in {generator.directory!s}/\" + model_name)\n\
if __name__ == \"__main__\":\n\
    run()"

    return input_string + output


################ Adding single model items

def add_service_plantuml(input_string: str, name: str, stereotypes: list, tagged_values: list):
    """Adds line for service to passed input string
    """

    new_line = "        " + name + " [label = \"{Process: " + name + " | "
    for stereotype in stereotypes:
        new_line += "--" + stereotype.strip() + "--\\n"
    if isinstance(tagged_values, dict):
        for tagged_value in tagged_values.keys():
            if "Endpoints" in tagged_value:
                new_line += str(tagged_value) + ": " + str(tagged_values[tagged_value]).replace("{", "\{").replace("}", "\}") + "\\n"
            else:
                new_line += str(tagged_value) + ": " + str(tagged_values[tagged_value]).replace("{", "\{").replace("}", "\}") + "\\n"
    else:
        for tagged_value in tagged_values:
            if ":" in tagged_value:
                new_line += tagged_value.split(":")[0].strip() + ": " + tagged_value.split(":")[1].strip().strip("\"").replace("{", "\{").replace("}", "\}") + "\\n"


    new_line += "}\" shape = Mrecord];\n"

    return input_string + new_line


def add_database_plantuml(input_string: str, name: str, stereotypes: list, tagged_values: list):
    """Adds line for database to passed input line.
    """

    new_line = "        " + name + " [label = \"|{Process: " + name + " | "
    for stereotype in stereotypes:
        new_line += "--" + stereotype.strip() + "--\\n"
    if isinstance(tagged_values, dict):
        for tagged_value in tagged_values.keys():
            if "Endpoints" in tagged_value:
                new_line += str(tagged_value) + ": " + str(tagged_values[tagged_value]) + "\\n"
            else:
                new_line += str(tagged_value) + ": " + str(tagged_values[tagged_value]) + "\\n"
    else:
        for tagged_value in tagged_values:
            if ":" in tagged_value:
                new_line += tagged_value.split(":")[0].strip() + ": " + tagged_value.split(":")[1].strip().strip("\"") + "\\n"
    new_line += "}\"]\n"

    return input_string + new_line


def add_external_entity_plantuml(input_string: str, name: str, stereotypes: list, tagged_values: list):
    """Adds line for database to passed input line.
    """

    new_line = "        " + name + " [label = \"{External Entity: " + name + " | "
    for stereotype in stereotypes:
        new_line += "--" + stereotype.strip() + "--\\n"
    if tagged_values:
        if isinstance(tagged_values, dict):
            for tagged_value in tagged_values.keys():
                if "Endpoints" in tagged_value:
                    new_line += str(tagged_value) + ": " + str(tagged_values[tagged_value]) + "\\n"
                else:
                    new_line += str(tagged_value) + ": " + str(tagged_values[tagged_value]) + "\\n"
        else:
            for tagged_value in tagged_values:
                if ":" in tagged_value:
                    new_line += tagged_value.split(":")[0].strip() + ": " + tagged_value.split(":")[1].strip().strip("\"") + "\\n"
    new_line += "}\"];\n"

    return input_string + new_line


def add_flow_plantuml(input_string: str, sender: str, receiver: str, stereotypes: list, tagged_values: list) -> str:
    """Adds line for information flow to the passed input string.
    """

    new_line = "        " + sender + " -> " + receiver + " [label = \" "
    for stereotype in stereotypes:
        new_line += "--" + stereotype.strip() + "--\\n"
    if tagged_values:
        if isinstance(tagged_values, dict):
            for tagged_value in tagged_values.keys():
                if "Endpoints" in tagged_value:
                    new_line += str(tagged_value) + ": " + str(tagged_values[tagged_value]) + "\\n"
                else:
                    new_line += str(tagged_value) + ": " + str(tagged_values[tagged_value]) + "\\n"
        else:
            for tagged_value in tagged_values:
                if ":" in tagged_value:
                    new_line += tagged_value.replace("\"", "") + "\\n"
    new_line += "\"]\n"

    return input_string + new_line


def add_service_codeable(output_lines: str, name: str, stereotypes: list, tagged_values: list) -> str:
    """Adds line for service in CodeableModels format.
    """

    tagged_values_string = "{"
    if isinstance(tagged_values, dict):
        for tagged_value in tagged_values:
            if "Port" in tagged_value:
                tagged_values_string += "\'" + tagged_value + "\': " + str(tagged_values[tagged_value]) + ", "
            else:
                tagged_values_string += "\'" + tagged_value + "\': \"" + str(tagged_values[tagged_value]) + "\", "
    else:
        for tagged_value in tagged_values:
            if "Port" in tagged_value:
                tagged_values_string += "\'" + str(tagged_value.split(":")[0]) + "\': " + str(tagged_value.split(": ")[1]) + ", "
            else:
                tagged_values_string += "\'" + str(tagged_value.split(":")[0]) + "\': \"" + str(tagged_value.split(": ")[1]) + "\", "
    tagged_values_string = tagged_values_string[:-2]
    tagged_values_string += "}"

    new_line = "\n" + name + " = CClass(service, \"" + name + "\", stereotype_instances = " + str(stereotypes).replace("\'", "").replace("\"", "") + ", tagged_values = " + tagged_values_string + ")"

    return output_lines + new_line


def add_database_codeable(output_lines: str, name: str, stereotypes: list, tagged_values: list) -> str:
    """Adds line for database in CodeableModels format.
    """

    tagged_values_string = "{"
    if isinstance(tagged_values, dict):
        for tagged_value in tagged_values:
            if "Port" in tagged_value:
                tagged_values_string += "\'" + tagged_value + "\': " + str(tagged_values[tagged_value]) + ", "
            else:
                tagged_values_string += "\'" + tagged_value + "\': \"" + str(tagged_values[tagged_value]) + "\", "
    else:
        for tagged_value in tagged_values:
            if "Port" in tagged_value:
                tagged_values_string += "\'" + str(tagged_value.split(":")[0]) + "\': " + str(tagged_value.split(": ")[1]) + ", "
            else:
                tagged_values_string += "\'" + str(tagged_value.split(":")[0]) + "\': \"" + str(tagged_value.split(": ")[1]) + "\", "
    tagged_values_string = tagged_values_string[:-2]
    tagged_values_string += "}"


    new_line = "\n" + name + " = CClass(database_component, \"" + name + "\", stereotype_instances = " + str(stereotypes).replace("\'", "").replace("\"", "") + ", tagged_values = " + tagged_values_string + ")"

    return output_lines + new_line


def add_external_entity_codeable(output_lines: str, name: str, stereotypes: list, tagged_values: list) -> str:
    """Adds line for external entity in CodeableModels format.
    """

    tagged_values_string = "{"
    if isinstance(tagged_values, dict):
        for tagged_value in tagged_values:
            if "Port" in tagged_value:
                tagged_values_string += "\'" + tagged_value + "\': " + str(tagged_values[tagged_value]) + ", "
            else:
                tagged_values_string += "\'" + tagged_value + "\': \"" + str(tagged_values[tagged_value]) + "\", "
    else:
        for tagged_value in tagged_values:
            if "Port" in tagged_value:
                tagged_values_string += "\'" + str(tagged_value.split(":")[0]) + "\': " + str(tagged_value.split(": ")[1]) + ", "
            else:
                tagged_values_string += "\'" + str(tagged_value.split(":")[0]) + "\': \"" + str(tagged_value.split(": ")[1]) + "\", "
    tagged_values_string = tagged_values_string[:-2]
    tagged_values_string += "}"

    new_line = "\n" + name + " = CClass(external_component, \"" + name + "\", stereotype_instances = " + str(stereotypes).replace("\'", "").replace("\"", "") + ", tagged_values = " + tagged_values_string + ")"

    return output_lines + new_line


def add_flow_codeable(output_lines: str, sender: str, receiver: str, stereotypes: list, tagged_values: list) -> str:
    """Adds line for information flow in CodeableModels format.
    """

    new_line = "\nadd_links({" + sender + ": " + receiver + "}, stereotype_instances = " + str(stereotypes).replace("\'", "").replace("\"", "")
    if tagged_values:
        tagged_values_string = "{"
        if isinstance(tagged_values, dict):
            for tagged_value in tagged_values:
                if "Port" in tagged_value:
                    tagged_values_string += "\'" + tagged_value + "\': " + str(tagged_values[tagged_value]) + ", "
                else:
                    tagged_values_string += "\'" + tagged_value + "\': \"" + str(tagged_values[tagged_value]) + "\", "
        else:
            for tagged_value in tagged_values:
                if "Port" in tagged_value:
                    tagged_values_string += "\'" + str(tagged_value.split(":")[0]) + "\': " + str(tagged_value.split(": ")[1]) + ", "
                else:
                    tagged_values_string += "\'" + str(tagged_value.split(":")[0]) + "\': \"" + str(tagged_value.split(": ")[1]) + "\", "
        tagged_values_string = tagged_values_string[:-2]
        tagged_values_string += "}"

        new_line += ", tagged_values = " + tagged_values_string
    new_line += ")"

    return output_lines + new_line


def add_service_json(output_dict: str, name: str, stereotypes: list, tagged_values: list):

    if not "services" in output_dict.keys():
        output_dict["services"] = list()

    tagged_values_dict = dict()
    for tagged_value in tagged_values:
        if "Port" in tagged_value:
            tagged_values_dict[str(tagged_value.split(":")[0])] = int(tagged_value.split(": ")[1])
        elif "Endpoints" in tagged_value:
            tagged_values_dict[str(tagged_value.split(":")[0])] = ast.literal_eval(tagged_value.split(": ")[1])
        else:
            tagged_values_dict[str(tagged_value.split(":")[0])] = str(tagged_value.split(": ")[1])

    output_dict["services"].append({"name": name, "stereotypes": stereotypes, "tagged_values": tagged_values_dict})

    return output_dict


def add_external_entity_json(output_dict: str, name: str, stereotypes: list, tagged_values: list):
    """Adds an external entity entry to the dict.
    """

    if not "external_entities" in output_dict.keys():
        output_dict["external_entities"] = list()

    tagged_values_dict = dict()
    for tagged_value in tagged_values:
        if "Port" in tagged_value:
            tagged_values_dict[str(tagged_value.split(":")[0])] = int(tagged_value.split(": ")[1])
        elif "Endpoints" in tagged_value:
            tagged_values_dict[str(tagged_value.split(":")[0])] = ast.literal_eval(tagged_value.split(": ")[1])
        else:
            tagged_values_dict[str(tagged_value.split(":")[0])] = str(tagged_value.split(": ")[1])

    output_dict["external_entities"].append({"name": name, "stereotypes": stereotypes, "tagged_values": tagged_values_dict})

    return output_dict


def add_flow_json(output_dict: str, sender: str, receiver: str, stereotypes: list, tagged_values: list):
    """Adds an information flow entry to the dict.
    """

    if not "information_flows" in output_dict.keys():
        output_dict["information_flows"] = list()

    tagged_values_dict = dict()
    for tagged_value in tagged_values:
        if "Port" in tagged_value:
            tagged_values_dict[str(tagged_value.split(":")[0])] = int(tagged_value.split(": ")[1])
        elif "Endpoints" in tagged_value:
            tagged_values_dict[str(tagged_value.split(":")[0])] = ast.literal_eval(tagged_value.split(": ")[1])
        else:
            tagged_values_dict[str(tagged_value.split(":")[0])] = str(tagged_value.split(": ")[1])

    output_dict["information_flows"].append({"sender": sender, "receiver": receiver, "stereotypes": stereotypes, "tagged_values": tagged_values_dict})

    return output_dict



################ Extracting single components

def parse_node_codeable(line: str):
    """Extracts name, stereotypes, and tagged values from input line in CodeableModels format
    """

    stereotypes, tagged_values = list(), list()

    name = line.split("=")[0].strip()
    if "stereotype_instances" in line:
        stereotype_part = line.split("stereotype_instances")[1].split("=")[1].strip()

        if stereotype_part[0] == "[":
            stereotypes = [item.strip() for item in stereotype_part.split("]")[0].split("[")[1].split(",")]
        else:
            stereotypes = list()
            stereotypes.append(stereotype_part.split(",")[0].strip(")").strip())
    if "tagged_values" in line:
        tagged_values_dict = ast.literal_eval(line.split("tagged_values =")[1].split(")")[0].strip())

        for tagged_value in tagged_values_dict:
            tagged_values.append(str(tagged_value) + ": " + str(tagged_values_dict[tagged_value]))
    return name, stereotypes, tagged_values



def parse_flow_codeable(line: str):
    """Extracts sender, receiver, sterotypes, and tagged valued from input line in CodeableModels format.
    """

    stereotypes, tagged_values = list(), list()

    sender = line.split("}")[0].split(":")[0].split("{")[1].strip()
    receiver = line.split("}")[0].split(":")[1].strip()

    if "stereotype_instances" in line:
        stereotype_part = line.split("stereotype_instances")[1].split("=")[1].strip()

        if stereotype_part[0] == "[":
            stereotypes = [item.strip() for item in stereotype_part.split("]")[0].split("[")[1].split(",")]
        else:
            stereotypes = list()
            stereotypes.append(stereotype_part.split(",")[0].strip(")").strip())
    if "tagged_values" in line:
        tagged_values = line.split("tagged_values =")[1].split("}")[0].split("{")[1].split(",")

    return sender, receiver, stereotypes, tagged_values


def parse_node_plantuml(line: str):
    """Extracts name, stereotypes, and tagged values from PlantUml line.
    """

    stereotypes, tagged_values = list(), list()

    name = line.split(":")[1].split("|")[0].strip()

    annotations = line.split("|")[1].split("];")[0].split("\\n")
    for annotation in annotations:
        if "--" in annotation:
            stereotypes.append(annotation.replace("-", "").strip())
        elif ":" in annotation:
            tagged_values.append(annotation.replace("\\", "").strip())

    return name, stereotypes, tagged_values


def parse_flow_plantuml(line: str):
    """Extracts sender, receiver, stereotypes, and tagged values from PlantUml line.
    """

    stereotypes, tagged_values = list(), list()

    sender = line.split("->")[0].strip()
    receiver = line.split("->")[1].split("[")[0].strip()

    annotations = line.split("label = ")[1].split("]")[0].split("\\n")
    for annotation in annotations:
        if "--" in annotation:
            stereotypes.append(annotation.replace("-", "").replace("\"", "").replace("\\n", "").strip())
        elif ":" in annotation:
            tagged_values.append(annotation.replace("\\", "").strip())

    return sender, receiver, stereotypes, tagged_values


################ Output
def write_output_json(output_file_path: str, content: str):
    """Writes output to JSON file.
    """

    with open(output_file_path, 'w') as output_file:
            json.dump(content, output_file, indent = 4)

    return 0


def write_output_text(output_file_path: str, content: str):
    """Writes the new format to a file.
    """

    with open(output_file_path, 'w') as output_file:
        output_file.write(content)

    return 0


################ Program flow

def convert(input, input_format: str, output_format: str, model_name: str):
    """Calls correct conversio function based on in- and output formats.
    Returns results.
    """

    if input_format == "py":
        if output_format == "py":
            print("Same format for in- and output, no conversion needed.")
            return 0
        elif output_format == "json":
            print("Converting from CodeableModels to JSON.")
            output = codeable_to_json(input)
        elif output_format == "txt":
            output = codeable_to_plantuml(input)
            print("Converting from CodeableModels to PlantUML.")
        else:
            print("Could not detect output format.")
            return 0
    elif input_format == "json":
        if output_format == "json":
            print("Same format for in- and output, no conversion needed.")
            return 0
        elif output_format == "py":
            output = json_to_codeable(input, model_name)
            print("Converting from JSON to CodeableModels.")
        elif output_format == "txt":
            output = json_to_plantuml(input)
            print("Converting from JSON to PlantUML.")
        else:
            print("Could not detect output format.")
            return 0
    elif input_format == "txt":
        if output_format == "txt":
            print("Same format for in- and output, no conversion needed.")
        elif output_format == "json":
            output = plantuml_to_json(input)
            print("Converting from PlantUML to JSON.")
        elif output_format == "py":
            output = plantuml_to_codeable(input, model_name)
            print("Converting from PlantUML to CodeableModels.")
        else:
            print("Could not detect output format.")
            return 0
    else:
        print("Could not detect input format.")
        return 0

    return output



def main():
    """Reads input path and output format from command line arguments, calls appropriate conversion function and writes output to provided path.
    """

    arguments = arg_parser.parse_args()

    input_path = arguments.input_path
    output_format = arguments.output_format

    input_format = input_path.split(".")[-1]

    if input_format == "json":
        with open(input_path, "r") as input_file:
            input = json.load(input_file)
    else:
        with open(input_path, 'r') as input_file:
            input = input_file.readlines()

    model_name = input_path.split("/")[-1].split(".")[0]

    output = convert(input, input_format, output_format, model_name)
    if output == 0:
        return

    if arguments.op:
        output_path = arguments.op
    else:
        output_path = "./converted/" + input_path.split("/")[-1].split(".")[0] + "." + output_format

    if output_format == "json":
        write_output_json(output_path, output)
    else:
        write_output_text(output_path, output)



if __name__ == '__main__':
    main()
