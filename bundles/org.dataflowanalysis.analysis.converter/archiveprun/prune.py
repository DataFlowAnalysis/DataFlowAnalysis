import os

def delete_files_with_specific_numbers_and_extensions(folder_path, numbers, extensions):
    for filename in os.listdir(folder_path):
        for number in numbers:
            for extension in extensions:
                if filename.endswith(f"_{number}.{extension}"):
                    file_path = os.path.join(folder_path, filename)
                    os.remove(file_path)
                    print(f"Deleted: {file_path}")

# Specify the folder path, numbers, and file extensions
folder_path = '.'
numbers = [3,6,9,10,12,13,14,15,16,17,18]  # Specify the numbers you want
extensions = ['json', 'datadictionary', 'dataflowdiagram']  # Specify the extensions you want

# Call the function to delete files
delete_files_with_specific_numbers_and_extensions(folder_path, numbers, extensions)

