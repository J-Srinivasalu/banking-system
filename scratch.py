import os

# Get the current working directory
current_directory = os.getcwd()

# Open a file to write the output
with open("file_paths.txt", "w") as f:
    # Walk through the directory and subdirectories
    for root, dirs, files in os.walk(current_directory):
        for file in files:
            # Get the relative path
            relative_path = os.path.relpath(os.path.join(root, file), current_directory)
            # Write the relative path to the file
            f.write(relative_path + "\n")
