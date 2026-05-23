IF OBJECT_ID(N'certifications', N'U') IS NULL
BEGIN
    CREATE TABLE certifications (
        driver_id                   UNIQUEIDENTIFIER  NOT NULL PRIMARY KEY,
        step                        INT               NOT NULL DEFAULT 1,
        basic_info_completed        BIT               NOT NULL DEFAULT 0,
        id_card_front_url           NVARCHAR(500)     NULL,
        id_card_front_status        NVARCHAR(20)      NOT NULL DEFAULT N'pending',
        id_card_back_url            NVARCHAR(500)     NULL,
        id_card_back_status         NVARCHAR(20)      NOT NULL DEFAULT N'pending',
        driver_license_url          NVARCHAR(500)     NULL,
        driver_license_status       NVARCHAR(20)      NOT NULL DEFAULT N'pending',
        criminal_record_url         NVARCHAR(500)     NULL,
        criminal_record_status      NVARCHAR(20)      NOT NULL DEFAULT N'pending',
        health_cert_url             NVARCHAR(500)     NULL,
        health_cert_status          NVARCHAR(20)      NOT NULL DEFAULT N'pending',
        vehicle_license_url         NVARCHAR(500)     NULL,
        vehicle_license_status      NVARCHAR(20)      NOT NULL DEFAULT N'pending',
        background_check_status     NVARCHAR(20)      NOT NULL DEFAULT N'pending',
        background_check_progress   INT               NOT NULL DEFAULT 0,
        updated_at                  DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_certifications_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
    );
END
GO

INSERT INTO certifications (driver_id, updated_at)
SELECT d.id, GETDATE()
FROM drivers d
WHERE NOT EXISTS (
    SELECT 1
    FROM certifications c
    WHERE c.driver_id = d.id
);
GO
