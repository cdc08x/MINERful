import subprocess
import os
import filecmp

# To run the unist tests run in MINERful directory pytest unit_tests/tests/MINERful_test.py

def test_minerful_discovery():
    script = './run-MINERful.sh'

    input_log = 'unit_tests/logs/ABC.txt'
    output_csv = 'unit_tests/specifications/generated-output.csv'
    expected_csv = 'unit_tests/expected_outcomes/expected.csv'

    if os.path.exists(output_csv):
        os.remove(output_csv)

    result = subprocess.run(
        [script, '-iLF', input_log, '-oCSV', output_csv, '-iLE', 'strings'],
        capture_output=True,
        text=True
    )

    assert result.returncode == 0, f"Failed:\n{result.stdout}\n{result.stderr}"
    assert os.path.exists(output_csv), f"Output file not created at {output_csv}"
    assert filecmp.cmp(output_csv, expected_csv, shallow=False), "Generated output does not match expected"


def test_event_log_maker_synthetic():
    script = './run-MINERfulEventLogMaker.sh'

    input_spec = 'unit_tests/specifications_synthetic/ABC.json'
    output_log = 'unit_tests/logs/ABC_synthetic.txt'
    expected_log = 'unit_tests/expected_outcomes/exp_ABC_synthetic.txt'

    if os.path.exists(output_log):
        os.remove(output_log)

    result = subprocess.run(
        [
            script,
            '--input-specification-file', input_spec,
            '--size', '1',
            '--minlen', '3',
            '--maxlen', '3',
            '--out-log-encoding', 'strings',
            '--out-log-file', output_log
        ],
        capture_output=True,
        text=True
    )

    assert result.returncode == 0, f"Failed:\n{result.stdout}\n{result.stderr}"
    assert os.path.exists(output_log), f"Output file not created at {output_log}"
    assert filecmp.cmp(output_log, expected_log, shallow=False), "Generated synthetic log does not match expected"


def test_fitness_checker():
    script = './run-MINERfulFitnessChecker.sh'

    input_log = 'unit_tests/logs/ABC_synthetic.xes'
    input_spec = 'unit_tests/specifications_synthetic/ABC.json'
    output_csv = 'unit_tests/specifications/generated_fitness.csv'
    expected_csv = 'unit_tests/expected_outcomes/exp_fitness.csv'

    if os.path.exists(output_csv):
        os.remove(output_csv)

    result = subprocess.run(
        [
            script,
            '-iLF', input_log,
            '-iLE', 'strings',
            '-iSF', input_spec,
            '-iSE', 'json',
            '-oCSV', output_csv
        ],
        capture_output=True,
        text=True
    )

    assert result.returncode == 0, f"Fitness checker failed:\n{result.stdout}\n{result.stderr}"
    assert os.path.exists(output_csv), f"Fitness output CSV not created at {output_csv}"
    assert filecmp.cmp(output_csv, expected_csv, shallow=False), "Generated fitness CSV does not match expected"


def test_slider():
    script = './run-MINERfulSlider.sh'

    input_log = 'unit_tests/logs/ABC.txt'
    output_csv = 'unit_tests/specifications/generated-output-slider.csv'
    expected_csv = 'unit_tests/expected_outcomes/exp_output_slider.csv'

    if os.path.exists(output_csv):
        os.remove(output_csv)

    result = subprocess.run(
        [
            script,
            '-iLF', input_log,
            '-iLE', 'strings',
            '-sliBy', '1',
            '-iLSubLen', '1',
            '-prune', 'none',
            '-sliOut', output_csv
        ],
        capture_output=True,
        text=True
    )

    if result.returncode != 0:
        print("STDOUT:\n", result.stdout)
        print("STDERR:\n", result.stderr)

    assert result.returncode == 0, f"Slider execution failed:\n{result.stdout}\n{result.stderr}"
    assert os.path.exists(output_csv), f"Slider output file was not created at {output_csv}"
    assert filecmp.cmp(output_csv, expected_csv, shallow=False), "Slider output CSV does not match expected"
