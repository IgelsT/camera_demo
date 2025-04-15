// function jsonToObject<T extends object>(data: any, type: { new (): T }): T {
//     let result = new type()

//     for (let [field, value] of Object.entries(result)) {
//         if (field in data) {
//             (result as any)[field] = (data as any)[field]
//         }
//     }
//     return result
// }
import md5 from 'md5';

const isMobile = {
  Android: function () {
    return navigator.userAgent.match(/Android/i);
  },
  BlackBerry: function () {
    return navigator.userAgent.match(/BlackBerry/i);
  },
  iOS: function () {
    return navigator.userAgent.match(/iPhone|iPad|iPod/i);
  },
  Opera: function () {
    return navigator.userAgent.match(/Opera Mini/i);
  },
  Windows: function () {
    return (
      navigator.userAgent.match(/IEMobile/i) ||
      navigator.userAgent.match(/WPDesktop/i)
    );
  },
  any: function () {
    return (
      isMobile.Android() ||
      isMobile.BlackBerry() ||
      isMobile.iOS() ||
      isMobile.Opera() ||
      isMobile.Windows()
    );
  },
};

function assingToExistField<T1 extends object, T2>(obj1: T1, obj2: T2): T1 {
  let result = {};
  for (const key in obj2) {
    const k = key as keyof typeof obj2;
    const updatingValue = obj2[k];
    if (obj1.hasOwnProperty(key)) {
      result = { ...result, [k]: updatingValue };
    }
  }
  return result as T1;
}

function hashString(str: string) {
  return md5(str);
}

// async function hashString1(str: string): Promise<string> {
//   console.log('in function before');
//   const enc = new TextEncoder();
//   const hash = await crypto.subtle.digest('SHA-1', enc.encode(str));
//   console.log('in function after');
//   return Array.from(new Uint8Array(hash))
//     .map((v) => v.toString(16).padStart(2, '0'))
//     .join('');
// }

export { assingToExistField, isMobile, hashString };
