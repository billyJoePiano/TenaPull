function compare(expected, actual) {
    let result = true;
    let aKeys = Object.keys(actual);
    let eKeys = Object.keys(expected);

    if (aKeys.length !== eKeys.length) fail("Number of keys", "length", eKeys, aKeys)
    for (let key of eKeys) {
        if (!aKeys.includes(key)) fail("Presence of key", key)
        let aType = typeof actual[key];
        let eType = typeof expected[key];
        if (aType !== eType) {
            fail("Types do not match", key);

        } else if (aType === "object" && actual[key] !== null && expected[key] !== null) {
            if (!compare(expected[key], actual[key])) fail("Objects do not match", key);

        } else {
            if (actual[key] !== expected[key]) fail("Values do not match", key);
        }
    }
    return result;

    function fail(reason, key, expectedVal = expected[key], actualVal = actual[key]) {
        console.error({ reason, key, actualVal, expectedVal });
        result = false;
    }

}