function compare(expected, actual, key) {
    if (key === undefined) {
        console.groupCollapsed({expected, actual});
    } else {
        console.groupCollapsed({key, expected, actual});
    }

    let result = true;
    let aKeys = Object.keys(actual);
    let eKeys = Object.keys(expected);

    let unexpected = aKeys.filter(str => !eKeys.includes(str));
    let missing = eKeys.filter(str => !aKeys.includes(str));

    if (unexpected.length > 0 || missing.length > 0) {
        fail("Mis-matched keys", {missing, unexpected}, eKeys, aKeys);
    }

    for (let key of eKeys) {
        if (!aKeys.includes(key)) continue;
        let aType = typeof actual[key];
        let eType = typeof expected[key];
        if (aType !== eType) {
            fail("Types do not match", key);

        } else if (aType === "object" && actual[key] !== null && expected[key] !== null) {
            if (!compare(expected[key], actual[key], key)) fail("Objects do not match", key);

        } else {
            if (actual[key] !== expected[key]) fail("Values do not match", key);
        }
    }

    console.groupEnd();

    return result;

    function fail(reason, key, expectedVal = expected[key], actualVal = actual[key]) {
        console.error({ reason, key, actualVal, expectedVal });
        result = false;
    }

}