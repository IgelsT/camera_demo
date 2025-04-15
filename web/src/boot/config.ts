class ConfigFields {
  api_url = '';
  LocalStorageString = '';
  hls_server = '';
}

const conf = new ConfigFields();

export default async () => {
  const res = await fetch('/appconfig/appconfig.conf', {
    cache: 'no-store',
  }).catch((error) => {
    console.log(error);
  });

  if (res instanceof Response) {
    const json = await res.json().catch((error) => {
      console.log(error);
    });
    Object.assign(conf, json);
  }
};

export { conf };
