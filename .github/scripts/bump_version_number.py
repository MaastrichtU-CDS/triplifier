import os
import toml
from pathlib import Path

pyproject = Path('pyproject.toml')
data = toml.load(pyproject)
version = data['project']['version']
major, minor, patch = map(int, version.split('.'))

ref = os.environ.get('GITHUB_REF', '')
if ref.startswith('refs/heads/version/'):
    patch += 1
    new_version = f'{major}.{minor}.{patch}'
else:
    minor += 1
    new_version = f'{major}.{minor}.0'

data['project']['version'] = new_version
with pyproject.open('w') as f:
    toml.dump(data, f)
with open(os.environ['GITHUB_OUTPUT'], 'a') as fh:
    print(f'new_version={new_version}', file=fh)